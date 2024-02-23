package com.hugo.ms.core.product.service;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hugo.api.core.product.ProductService;
import com.hugo.api.event.Event;
import com.hugo.api.core.exception.EventProcessingException;
import com.hugo.api.core.product.Product;


@Configuration
public class MessageProcessorConfig {

	private static final Logger LOG= LoggerFactory.getLogger(MessageProcessorConfig.class);
	
	private final ProductService productService;
	
	@Autowired
	public MessageProcessorConfig(ProductService productService) {
		this.productService=productService;
		
	}
	
	@Bean
	public Consumer<Event<Integer, Product>> messageProcessor(){
		return event ->{
			LOG.info("Process message created at {}...", event.getEventCreatedAt());
			
			switch(event.getEventType()) {
				case CREATE:
					Product product=event.getData();
					LOG.info("Create product with ID: {}", product.getProductId());
					productService.createProduct(product).block();
					break;
				case DELETE:
					int productId=event.getKey();
					LOG.info("Delete product with ProductID: {}", productId);
					productService.deleteProduct(productId).block();
					break;
				default:
					String errorMessage="Incorrect event type: "+event.getEventType();
					LOG.warn(errorMessage);
					throw new EventProcessingException();
			}
			LOG.info("Message processing done!!");
		};
	}
	
}
