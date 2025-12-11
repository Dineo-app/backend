package com.dineo_backend.dineo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync  // ← STEP 1: This creates a THREAD POOL when app starts
              //   Default: 8 worker threads ready to execute async tasks
              //   Thread names: "task-1", "task-2", "task-3", etc.
              // 
              // Spring Batch is automatically enabled via spring-boot-starter-batch
              // It creates additional thread pools for batch processing:
              // - "email-batch-1", "email-batch-2", etc. (4 threads for email batches)
public class DineoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DineoApplication.class, args);
		
		// When app starts, multiple thread pools are created:
		// 1. HTTP Thread Pool: Handles incoming requests (e.g., "http-nio-8080-exec-1")
		// 2. Async Thread Pool: Handles @Async methods (e.g., "task-1", "task-2")
		// 3. Batch Thread Pool: Handles batch jobs (e.g., "email-batch-1" to "email-batch-4")
	}

}
