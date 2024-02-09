package com.hugo.ms.core.product.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.hugo.api.core.product.Product;
import com.hugo.ms.core.product.persistence.ProductEntity;

@Mapper(componentModel="spring")
public interface ProductMapper {

	@Mappings({
		@Mapping(target="serviceAddress", ignore=true)
	})
	Product entityToApi(ProductEntity entity);
	
	@Mappings({
		@Mapping(target="id", ignore=true), 
		@Mapping(target="version", ignore=true)
	})
	ProductEntity apiToEntity(Product api);
	
}
