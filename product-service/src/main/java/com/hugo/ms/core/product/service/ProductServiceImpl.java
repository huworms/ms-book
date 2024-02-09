package com.hugo.ms.core.product.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.hugo.api.core.exception.InvalidInputException;
import com.hugo.api.core.exception.NotFoundException;
import com.hugo.api.core.product.Product;
import com.hugo.api.core.product.ProductService;
import com.hugo.ms.core.product.persistence.ProductEntity;
import com.hugo.ms.core.product.persistence.ProductRepository;
import com.hugo.util.ServiceUtil;
import com.mongodb.DuplicateKeyException;

@RestController
public class ProductServiceImpl implements ProductService{

	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
	
	private final ServiceUtil serviceUtil;
	private final ProductRepository repository;
	private final ProductMapper mapper;
	
	
	@Autowired
	public ProductServiceImpl(
			ProductRepository repository,
			ProductMapper mapper,
			ServiceUtil serviceUtil) {
		this.serviceUtil=serviceUtil;
		this.mapper=mapper;
		this.repository=repository;
		
	}
	
	@Override
	public Product getProduct(int productId) {
		 LOG.debug("/product return the found product for productId={}", productId);
		 if (productId < 1) {
		      throw new InvalidInputException("Invalid productId: " + productId);
		    }

		    if (productId == 13) {
		      throw new NotFoundException("No product found for productId: " + productId);
		    }
		    
		  ProductEntity entity= repository.findByProductId(productId)
				  	.orElseThrow(()-> new NotFoundException("No product found for productId:" + productId));
		   
		  Product response=mapper.entityToApi(entity);
		  response.setServiceAddress(serviceUtil.getServiceAddress());
		  
		  LOG.debug("getProduct:  found productId: {}", response.getProductId());
		return response;
		
	}

	@Override
	public Product createProduct(Product body) {
		try
		{
			ProductEntity entity= mapper.apiToEntity(body);
			ProductEntity newEntity=this.repository.save(entity);
			LOG.debug("createProduct: entity created for productId: {}", body.getProductId());
			
			return mapper.entityToApi(newEntity);
		}catch(DuplicateKeyException dke) {
			throw new InvalidInputException("Duplicate key, Product Id: "+body.getProductId());
		}
	}

	@Override
	public void deleteProduct(int productId) {
		LOG.debug("deleteProduct: tries to delete an entity with  with productId: {}", productId);
		repository.findByProductId(productId).ifPresent(e->this.repository.delete(e));
		
	}

}
