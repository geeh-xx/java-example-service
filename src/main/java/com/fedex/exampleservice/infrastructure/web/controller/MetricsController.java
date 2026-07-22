package com.fedex.exampleservice.infrastructure.web.controller;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fedex.exampleservice.infrastructure.web.api.MetricsApi;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController implements MetricsApi {

	private static final String UP = "UP";

	private static final String DOWN = "DOWN";

	private static final String UNKNOWN = "UNKNOWN";

	private final Environment environment;

	private final DataSource dataSource;

	private final PrometheusMeterRegistry prometheusMeterRegistry;

	private final MeterRegistry meterRegistry;

	private final AtomicInteger activeConnections = new AtomicInteger(0);

	private Counter healthCheckCounter;

	private Counter databaseConnectionTotalCounter;

	private Counter databaseConnectionSuccessCounter;

	private Counter databaseConnectionFailureCounter;

	private Timer databaseConnectionTimer;

	public MetricsController(Environment environment, @Autowired(required = false) DataSource dataSource,
			@Autowired(required = false) PrometheusMeterRegistry prometheusMeterRegistry,
			@Autowired(required = false) MeterRegistry meterRegistry) {
		this.environment = environment;
		this.dataSource = dataSource;
		this.prometheusMeterRegistry = prometheusMeterRegistry;
		this.meterRegistry = meterRegistry;
	}

	private static double getUsedMemory(MetricsController controller) {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	private static double getUptimeInSeconds(MetricsController controller) {
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		return runtimeMxBean.getUptime() / 1000.0;
	}

	@PostConstruct
	public void initMetrics() {
		if (this.meterRegistry == null) {
			return;
		}

		this.healthCheckCounter = Counter.builder("health.check.count")
			.description("Total number of health checks performed")
			.tag("endpoint", "health")
			.register(this.meterRegistry);

		this.databaseConnectionTotalCounter = Counter.builder("database.connection.attempts.total")
			.description("Total database connection attempts")
			.register(this.meterRegistry);

		this.databaseConnectionSuccessCounter = Counter.builder("database.connection.attempts.success")
			.description("Total successful database connection attempts")
			.register(this.meterRegistry);

		this.databaseConnectionFailureCounter = Counter.builder("database.connection.attempts.failed")
			.description("Total failed database connection attempts")
			.register(this.meterRegistry);

		this.databaseConnectionTimer = Timer.builder("database.connection.duration")
			.description("Database connection duration")
			.register(this.meterRegistry);

		Gauge.builder("jvm.memory.used.custom", this, MetricsController::getUsedMemory)
			.description("Custom JVM memory used")
			.register(this.meterRegistry);

		Gauge.builder("database.connections.active", this.activeConnections, AtomicInteger::get)
			.description("Active database connections")
			.register(this.meterRegistry);

		Gauge.builder("application.uptime.seconds", this, MetricsController::getUptimeInSeconds)
			.description("Application uptime in seconds")
			.register(this.meterRegistry);

		Gauge.builder("database.health.status", this, (controller) -> isDatabaseHealthy() ? 1 : 0)
			.description("Database health status (1=UP, 0=DOWN)")
			.register(this.meterRegistry);
	}

	@Override
	public ResponseEntity<String> prometheus() {
		if (this.prometheusMeterRegistry != null) {
			return ResponseEntity.ok(this.prometheusMeterRegistry.scrape());
		}
		return ResponseEntity.ok("# Prometheus metrics not available\n");
	}

	@Override
	public ResponseEntity<Map<String, Object>> health() {
		if (this.healthCheckCounter != null) {
			this.healthCheckCounter.increment();
		}

		Map<String, Object> healthResponse = new HashMap<>();
		Map<String, Object> components = new HashMap<>();
		boolean isHealthy = true;

		Map<String, Object> databaseHealth = checkDatabaseHealth();
		components.put("db", databaseHealth);
		if (!UP.equals(databaseHealth.get("status"))) {
			isHealthy = false;
		}

		Map<String, Object> diskHealth = checkDiskSpaceHealth();
		components.put("diskSpace", diskHealth);
		if (!UP.equals(diskHealth.get("status"))) {
			isHealthy = false;
		}

		if (this.meterRegistry != null) {
			Map<String, Object> metricsInfo = new HashMap<>();
			metricsInfo.put("status", UP);
			metricsInfo.put("details", Map.of("registeredMeters", this.meterRegistry.getMeters().size(),
					"prometheusEnabled", this.prometheusMeterRegistry != null));
			components.put("metrics", metricsInfo);
		}

		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		healthResponse.put("status", isHealthy ? UP : DOWN);
		healthResponse.put("components", components);
		healthResponse.put("details", Map.of("uptime", runtimeMxBean.getUptime()));

		return ResponseEntity.ok(healthResponse);
	}

	@Override
	public ResponseEntity<Map<String, Object>> env() {
		Map<String, Object> envResponse = new HashMap<>();
		Map<String, Object> propertySources = new HashMap<>();

		Map<String, Object> systemProperties = new HashMap<>();
		systemProperties.put("java.version", System.getProperty("java.version"));
		systemProperties.put("java.vendor", System.getProperty("java.vendor"));
		systemProperties.put("os.name", System.getProperty("os.name"));
		systemProperties.put("os.version", System.getProperty("os.version"));
		systemProperties.put("user.timezone", System.getProperty("user.timezone"));

		Map<String, Object> applicationProperties = new HashMap<>();
		applicationProperties.put("server.port", this.environment.getProperty("server.port", "8080"));
		applicationProperties.put("spring.application.name",
				this.environment.getProperty("spring.application.name", "app"));

		propertySources.put("systemProperties", Map.of("properties", systemProperties));
		propertySources.put("applicationConfig", Map.of("properties", applicationProperties));
		envResponse.put("propertySources", propertySources);

		return ResponseEntity.ok(envResponse);
	}

	@Override
	public ResponseEntity<Map<String, Object>> metrics() {
		Map<String, Object> metricsResponse = new HashMap<>();

		if (this.meterRegistry != null) {
			String[] metricNames = this.meterRegistry.getMeters()
				.stream()
				.map((meter) -> meter.getId().getName())
				.distinct()
				.sorted()
				.toArray(String[]::new);

			metricsResponse.put("names", metricNames);
			metricsResponse.put("totalMeters", this.meterRegistry.getMeters().size());
			metricsResponse.put("prometheusEnabled", this.prometheusMeterRegistry != null);

			if (this.prometheusMeterRegistry != null) {
				metricsResponse.put("prometheusEndpoint", "/actuator/prometheus");
			}

			return ResponseEntity.ok(metricsResponse);
		}

		String[] metricNames = { "jvm.memory.used", "jvm.memory.max", "jvm.threads.live", "process.uptime",
				"system.cpu.usage", "process.cpu.usage" };
		metricsResponse.put("names", metricNames);
		return ResponseEntity.ok(metricsResponse);
	}

	@Override
	public ResponseEntity<Map<String, Object>> jvmMemoryUsed() {
		Map<String, Object> metric = new HashMap<>();
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = runtime.totalMemory() - runtime.freeMemory();

		metric.put("name", "jvm.memory.used");
		metric.put("description", "The amount of used memory");
		metric.put("baseUnit", "bytes");
		metric.put("measurements", Map.of("value", usedMemory));

		return ResponseEntity.ok(metric);
	}

	@Override
	public ResponseEntity<Map<String, Object>> processUptime() {
		Map<String, Object> metric = new HashMap<>();
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

		metric.put("name", "process.uptime");
		metric.put("description", "The uptime of the Java virtual machine");
		metric.put("baseUnit", "seconds");
		metric.put("measurements", Map.of("value", runtimeMxBean.getUptime() / 1000.0));

		return ResponseEntity.ok(metric);
	}

	@Override
	public ResponseEntity<Map<String, Object>> readiness() {
		Map<String, Object> readinessResponse = new HashMap<>();
		Map<String, Object> dbHealth = checkDatabaseHealth();
		boolean isReady = UP.equals(dbHealth.get("status"));

		readinessResponse.put("status", isReady ? UP : DOWN);
		readinessResponse.put("details", Map.of("database", dbHealth));
		return ResponseEntity.ok(readinessResponse);
	}

	@Override
	public ResponseEntity<Map<String, Object>> liveness() {
		Map<String, Object> livenessResponse = new HashMap<>();
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

		livenessResponse.put("status", UP);
		livenessResponse.put("details", Map.of("uptime", runtimeMxBean.getUptime()));
		return ResponseEntity.ok(livenessResponse);
	}

	private Map<String, Object> checkDatabaseHealth() {
		Map<String, Object> databaseHealth = new HashMap<>();

		if (this.dataSource == null) {
			databaseHealth.put("status", UNKNOWN);
			databaseHealth.put("details", Map.of("error", "DataSource not configured"));
			return databaseHealth;
		}

		Timer.Sample sample = null;
		if (this.databaseConnectionTimer != null && this.meterRegistry != null) {
			sample = Timer.start(this.meterRegistry);
		}

		boolean connectionOpened = false;
		try (Connection connection = this.dataSource.getConnection()) {
			connectionOpened = true;
			this.activeConnections.incrementAndGet();

			if (this.databaseConnectionTotalCounter != null) {
				this.databaseConnectionTotalCounter.increment();
			}

			if (connection.isValid(5)) {
				databaseHealth.put("status", UP);
				databaseHealth.put("details", Map.of("database", connection.getMetaData().getDatabaseProductName(),
						"validationQuery", "isValid()"));
				if (this.databaseConnectionSuccessCounter != null) {
					this.databaseConnectionSuccessCounter.increment();
				}
			}
			else {
				databaseHealth.put("status", DOWN);
				databaseHealth.put("details", Map.of("error", "Connection validation failed"));
				if (this.databaseConnectionFailureCounter != null) {
					this.databaseConnectionFailureCounter.increment();
				}
			}
		}
		catch (SQLException exception) {
			databaseHealth.put("status", DOWN);
			databaseHealth.put("details", Map.of("error", exception.getMessage()));
			if (this.databaseConnectionFailureCounter != null) {
				this.databaseConnectionFailureCounter.increment();
			}
		}
		finally {
			if (connectionOpened) {
				this.activeConnections.decrementAndGet();
			}
			if (sample != null && this.databaseConnectionTimer != null) {
				sample.stop(this.databaseConnectionTimer);
			}
		}

		return databaseHealth;
	}

	private Map<String, Object> checkDiskSpaceHealth() {
		Map<String, Object> diskHealth = new HashMap<>();

		try {
			long freeSpace = new File(".").getFreeSpace();
			long totalSpace = new File(".").getTotalSpace();
			long threshold = 10L * 1024 * 1024 * 1024;

			if (freeSpace > threshold) {
				diskHealth.put("status", UP);
				diskHealth.put("details", Map.of("free", freeSpace, "total", totalSpace, "threshold", threshold));
			}
			else {
				diskHealth.put("status", DOWN);
				diskHealth.put("details", Map.of("free", freeSpace, "total", totalSpace, "threshold", threshold,
						"error", "Free disk space below threshold"));
			}
		}
		catch (Exception exception) {
			diskHealth.put("status", DOWN);
			diskHealth.put("details", Map.of("error", exception.getMessage()));
		}

		return diskHealth;
	}

	private boolean isDatabaseHealthy() {
		if (this.dataSource == null) {
			return false;
		}

		try (Connection connection = this.dataSource.getConnection()) {
			return connection.isValid(5);
		}
		catch (SQLException exception) {
			return false;
		}
	}

}
