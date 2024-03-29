package com.hugo.ms.composite.product.services;
import static java.util.logging.Level.FINE;
import static com.hugo.api.event.Event.Type.CREATE;
import static com.hugo.api.event.Event.Type.DELETE;
import static reactor.core.publisher.Flux.empty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hugo.api.core.exception.InvalidInputException;
import com.hugo.api.core.exception.NotFoundException;
import com.hugo.api.core.product.Product;
import com.hugo.api.core.product.ProductService;
import com.hugo.api.core.recommendation.Recommendation;
import com.hugo.api.core.recommendation.RecommendationService;
import com.hugo.api.core.review.Review;
import com.hugo.api.core.review.ReviewService;
import com.hugo.api.event.Event;
import com.hugo.util.HttpErrorInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService{
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
	
	
	private final WebClient webClient;
	private final ObjectMapper mapper;
	private final String productServiceUrl;
	private final String recommendationServiceUrl;
	private final String reviewServiceUrl;
	
	private final StreamBridge streamBridge;
	private final Scheduler publishEventScheduler;
	
	
	 @Autowired
	  public ProductCompositeIntegration(
		@Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
	    WebClient.Builder webClient,
	    ObjectMapper mapper,
	    StreamBridge streamBridge,
	    
	    @Value("${app.product-service.host}") String productServiceHost,
	    @Value("${app.product-service.port}") int productServicePort,
	    @Value("${app.recommendation-service.host}") String recommendationServiceHost,
	    @Value("${app.recommendation-service.port}") int recommendationServicePort,
	    @Value("${app.review-service.host}") String reviewServiceHost,
	    @Value("${app.review-service.port}") int reviewServicePort) {
		 
		 this.publishEventScheduler=publishEventScheduler;
		 this.streamBridge=streamBridge;
		 this.webClient=webClient.build();
		 this.mapper = mapper;

		 productServiceUrl = "http://" + productServiceHost + ":" + productServicePort;
		 recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
		 reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort;
		 
	 }
	 
	@Override
	public Mono<Product> createProduct(Product body) {
		
		return Mono.fromCallable(()->{
			sendMessage("products-out-0", new Event(CREATE, body.getProductId(),body ));
			return body;
		}).subscribeOn(publishEventScheduler);
		
	}
		 

	@Override
	public Mono<Product> getProduct(int productId) {
		  LOG.info("productId: "+productId);
	      String url = productServiceUrl +"/product/"+ productId;
	      LOG.debug("Will call getProduct API on URL: {}", url);
	      LOG.info("url: "+url);
	      
	      return webClient.get().uri(url)
	    		  .retrieve()
	    		  .bodyToMono(Product.class)
	    		  .log(LOG.getName(), FINE)
	    		  .onErrorMap(WebClientResponseException.class, ex-> handleException(ex));
	    	
	   }
	
	@Override
	public Mono<Void> deleteProduct(int productId) {
		return Mono.fromRunnable(()-> sendMessage("products-out-0", new Event(DELETE, productId,null)))
				.subscribeOn(publishEventScheduler).then();
	}
	 
	@Override
	public Mono<Recommendation> createRecommendation(Recommendation body) {
		return Mono.fromCallable(()->{
			sendMessage("recommendations-out-0", 
					new Event(CREATE, body.getProductId(), body));
			return body;
		}).subscribeOn(publishEventScheduler);
		
	}
	
	@Override
	public Flux<Recommendation> getRecommendations(int productId) {
		
	      String url = recommendationServiceUrl + "/recommendation?productId="+productId;

	      LOG.info("Will call getRecommendations API on URL: {}", url);
	      return webClient.get().uri(url)
	    		  .retrieve()
	    		  .bodyToFlux(Recommendation.class)
	    		  .log(LOG.getName(),FINE)
	    		  .onErrorResume(error-> empty());
	}


	@Override
	public Mono<Void> deleteRecommendations(int productId) {
		return Mono.fromRunnable(()->sendMessage("recommendations-out-0", 
				new Event(DELETE, productId, null)))
				.subscribeOn(publishEventScheduler).then();
	}

	
	@Override
	public Mono<Review> createReview(Review body) {
		return Mono.fromCallable(()-> {
			sendMessage("reviews-out-0", 
					new Event(CREATE, body.getProductId(), body));
			return body;
		}).subscribeOn(publishEventScheduler);
	}
	
	@Override
	public Flux<Review> getReviews(int productId) {
		 
		      String url = reviewServiceUrl +"/review?productId="+ productId;
		      LOG.info("Will call getReviews API on URL: {}", url);
		      // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
		      return webClient.get().uri(url)
		    		  	.retrieve()
		    		  	.bodyToFlux(Review.class)
		    		  	.log(LOG.getName(), FINE)
		    		  	.onErrorResume(error-> empty());
	}
	

	@Override
	public Mono<Void> deleteReviews(int productId) {
		return Mono.fromRunnable(()-> sendMessage("reviews-out-0", 
				new Event(DELETE, productId, null)))
				.subscribeOn(publishEventScheduler).then();	
	}
	
	
	private void sendMessage(String bindinName, Event event) {
		LOG.debug("Sending a {} message to {}", event.getEventType());
		Message message= MessageBuilder.withPayload(event)
				.setHeader("partitionKey", event.getKey())
				.build();
		streamBridge.send(bindinName, message);
		
	}
	
	public Mono<Health> getProductHealth() {
	    return getHealth(productServiceUrl);
	}
	
	public Mono<Health> getRecommendationHealth() {
	    return getHealth(recommendationServiceUrl);
	}
	
	public Mono<Health> getReviewHealth() {
	    return getHealth(reviewServiceUrl);
	} 
	
	private Mono<Health> getHealth(String url){
		url+="/actuator/health";
		LOG.debug("Will call the Health API on ULR : {}", url);
		return webClient.get().uri(url).retrieve().bodyToMono(String.class)
				.map(s-> new Health.Builder().up().build())
				.onErrorResume(ex-> Mono.just(new Health.Builder().down(ex).build()))
				.log(LOG.getName(), FINE);
	}
	
	 private String getErrorMessage(WebClientResponseException ex) {
	    try {
	      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
	    } catch (IOException ioex) {
	      return ex.getMessage();
	    }
	  }
	
	private Throwable handleException(Throwable ex) {
		
		if(!(ex instanceof WebClientResponseException)) {
			LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
		}
		
		WebClientResponseException wcre=(WebClientResponseException)ex;
		
	    switch (HttpStatus.resolve(wcre.getStatusCode().value())) {

	      case NOT_FOUND:
	        return new NotFoundException(getErrorMessage(wcre));

	      case UNPROCESSABLE_ENTITY:
	        return new InvalidInputException(getErrorMessage(wcre));

	      default:
	        LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
	        LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
	        return ex;
	    }
	  }




	

}
