package com.lambakean.RationPlanner.domain.service.impl;

import com.lambakean.RationPlanner.data.model.MeasurementUnit;
import com.lambakean.RationPlanner.data.model.Product;
import com.lambakean.RationPlanner.data.model.ProductQuantity;
import com.lambakean.RationPlanner.data.model.User;
import com.lambakean.RationPlanner.data.repository.MeasurementUnitRepository;
import com.lambakean.RationPlanner.data.repository.ProductRepository;
import com.lambakean.RationPlanner.domain.exception.AccessDeniedException;
import com.lambakean.RationPlanner.domain.exception.BadRequestException;
import com.lambakean.RationPlanner.domain.exception.EntityNotFoundException;
import com.lambakean.RationPlanner.domain.service.PrincipalService;
import com.lambakean.RationPlanner.domain.service.ProductService;
import com.lambakean.RationPlanner.domain.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final PrincipalService principalService;

    private final ValidationService validationService;
    private final Validator productQuantityValidator;
    private final Validator productValidator;

    @Override
    public Product getProductById(String id) {

        Product product = productRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Продукт с id [%s] не существует", id))
        );

        User user = principalService.getPrincipal();

        if(!user.getId().equals(product.getUserId())) {
            throw new AccessDeniedException("Вы не имеете доступа к этому продукту.");
        }

        return product;
    }

    @Override
    @Transactional
    public List<Product> getCurrentUserProducts() {

        User user = principalService.getPrincipal();

        return productRepository.getAllByUser(user);
    }

    @Override
    public Product createProduct(@NonNull Product productData) {

        User user = principalService.getPrincipal();

        String measurementUnitId = productData.getMeasurementUnitId();

        if(measurementUnitId == null) {
            throw new BadRequestException("Вы не выбрали единицу измерения количества продукта");
        }

        MeasurementUnit measurementUnit = measurementUnitRepository.findById(measurementUnitId).orElseThrow(
                () -> new EntityNotFoundException("Вы выбрали невалидную единицу измерения количества продукта")
        );

        Double productQuantityAmount = productData.getQuantityAmount();
        ProductQuantity productQuantity = new ProductQuantity(productQuantityAmount, measurementUnit);

        validationService.validateThrowExceptionIfInvalid(productQuantity, productQuantityValidator);

        productData.setUser(user);
        productData.setQuantity(productQuantity);

        validationService.validateThrowExceptionIfInvalid(productData, productValidator);

        productRepository.saveAndFlush(productData);

        return productData;
    }

    @Override
    public void deleteProductById(String id) {

        User user = principalService.getPrincipal();

        if(id == null || !productRepository.existsByIdAndUser(id, user)) {
            throw new EntityNotFoundException("Неверно указан идентификатор продукта, который вы хотите удалить");
        }

        productRepository.deleteById(id);
    }
}