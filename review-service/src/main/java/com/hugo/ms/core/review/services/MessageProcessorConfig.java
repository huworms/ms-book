package com.hugo.ms.core.review.services;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hugo.api.core.exception.EventProcessingException;
import com.hugo.api.core.review.Review;
import com.hugo.api.core.review.ReviewService;
import com.hugo.api.event.Event;

@Configuration
public class MessageProcessorConfig {
	
	private static final Logger LOG= LoggerFactory.getLogger(MessageProcessorConfig.class);
	
	private final ReviewService reviewService;
	
	@Autowired
	public MessageProcessorConfig(ReviewService reviewService) {
		this.reviewService=reviewService;
	}
	
	@Bean
	public Consumer<Event<Integer, Review>> messageProcessor(){
		return event->{
			LOG.info("Process message created at {}..."+event.getEventCreatedAt());
			
			switch(event.getEventType()) {
				case CREATE:
					Review review= event.getData();
					LOG.info("Create review with ID: {}/{}", review.getProductId(), review.getReviewId());
					reviewService.createReview(review).block();
					break;
				case DELETE:
					int productId= event.getKey();
					LOG.info("Delete reviews with ProductID: {}", productId);
					reviewService.deleteReviews(productId).block();
					break;
				default:
					String errorMessage="Incorrect event type: "+event.getEventType();
					LOG.warn(errorMessage);
					throw new EventProcessingException(errorMessage);
			}
			LOG.info("Messsage processing done!");
		};
	}

}
