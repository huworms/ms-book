package com.hugo.ms.composite.product.services;
import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
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
import com.hugo.util.HttpErrorInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService{
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
	
	private final WebClient webClient;
	private final ObjectMapper mapper;
	private final String productServiceUrl;
	private final String recommendationServiceUrl;
	private final String reviewServiceUrl;
	
	
	 @Autowired
	  public ProductCompositeIntegration(
	    WebClient.Builder webClient,
	    ObjectMapper mapper,
	    @Value("${app.product-service.host}") String productServiceHost,
	    @Value("${app.product-service.port}") int productServicePort,
	    @Value("${app.recommendation-service.host}") String recommendationServiceHost,
	    @Value("${app.recommendation-service.port}") int recommendationServicePort,
	    @Value("${app.review-service.host}") String reviewServiceHost,
	    @Value("${app.review-service.port}") int reviewServicePort) {
		 
		 this.webClient=webClient.build();
		 this.mapper = mapper;

		 productServiceUrl = "http://" + productServiceHost + ":" + productServicePort;
		 recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
		 reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort;
		 
	 }
	 
	@Override
	public Mono<Product> createProduct(Product body) {
		try {
			String url=productServiceUrl;
			LOG.debug("will post a new product to URL: {}",url);
			Product product=restTemplate.postForObject(url, body,Product.class);
			LOG.debug("Created a product with id: {}",product.getProductId());
			return product;
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
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
	public void deleteProduct(int productId) {
		try {
			String url= productServiceUrl+"/"+productId;
			LOG.debug("Will call the deleteProduct API on URL: {}", url);
			restTemplate.delete(url);
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}		
	}
	 
	@Override
	public Recommendation createRecommendation(Recommendation body) {
		try {
			String url=recommendationServiceUrl;
			LOG.debug("Will post a new recommendation to URL: {}", url);
			
			Recommendation recommendation=restTemplate.postForObject(url, body, Recommendation.class);
			LOG.debug("Created a recommendation with id: {}", recommendation.getProductId());
			
			return recommendation;
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
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
	public void deleteRecommendations(int productId) {
		try {
			String url= recommendationServiceUrl+"?productId="+productId;
			LOG.debug("Will call the deleteRecommendations API on URL: {}",url);
			
			restTemplate.delete(url);
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
	}

	
	@Override
	public Review createReview(Review body) {
		try {
			String url=reviewServiceUrl;
			LOG.debug("Will post a new review to URL: {}", url);
			
			Review review= restTemplate.postForObject(url, body, Review.class);
			LOG.debug("Created a review with id: {}", review.getProductId());
			return review;
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
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
	public void deleteReviews(int productId) {
		try {
			String url= reviewServiceUrl+"?productId="+productId;
			LOG.debug("Will call the deleteReview API ON URL: {}", url);
			
			restTemplate.delete(url);
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
		
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
