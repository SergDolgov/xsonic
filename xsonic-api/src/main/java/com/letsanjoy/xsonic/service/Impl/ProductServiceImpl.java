package com.letsanjoy.xsonic.service.Impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.letsanjoy.xsonic.domain.Product;
import com.letsanjoy.xsonic.dto.product.ProductSearchRequest;
import com.letsanjoy.xsonic.enums.SearchProduct;
import com.letsanjoy.xsonic.exception.ApiRequestException;
import com.letsanjoy.xsonic.repository.ProductRepository;
import com.letsanjoy.xsonic.repository.projection.FullProductProjection;
import com.letsanjoy.xsonic.repository.projection.ProductProjection;
import com.letsanjoy.xsonic.service.ProductService;
import com.letsanjoy.xsonic.constants.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AmazonS3 amazonS3client;

    @Value("${amazon.s3.bucket.name}")
    private String bucketName;

    @Value("${upload.path}")
    private Resource uploadPath;

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ApiRequestException(ErrorMessage.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    public Page<FullProductProjection> getAdminProducts(Pageable pageable) {
        return productRepository.findAdminAllByOrderByIdAsc(pageable);
    }

    @Override
    public Page<ProductProjection> getAllProducts(Pageable pageable) {
        return productRepository.findAllByOrderByIdAsc(pageable);
    }

    @Override
    public List<ProductProjection> getProductsByIds(List<Long> productsId) {
        return productRepository.getProductsByIds(productsId);
    }

    @Override
    public Page<ProductProjection> findProductsByFilterParams(ProductSearchRequest filter, Pageable pageable) {
        return productRepository.findProductsByFilterParams(
                filter.getBrands(),
                filter.getCategories(),
                filter.getPrices().get(0),
                filter.getPrices().get(1),
                filter.getSortByPrice(),
                pageable);
    }

    @Override
    public List<Product> findByBrand(String brand) {
        return productRepository.findByBrandOrderByPriceDesc(brand);
    }

    @Override
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategoryOrderByPriceDesc(category);
    }

    @Override
    public Page<ProductProjection> findByInputText(SearchProduct searchType, String text, Pageable pageable) {
        if (searchType.equals(SearchProduct.BRAND)) {
            return productRepository.findByBrand(text, pageable);
        } else if (searchType.equals(SearchProduct.CATEGORY)) {
            return productRepository.findByCategory(text, pageable);
        } else if (searchType.equals(SearchProduct.TITLE)) {
            return productRepository.findByTitle(text, pageable);
        } else {
            return productRepository.findByCategory(text, pageable);
        }
    }

    @Override
    @Transactional
    public Product saveProduct(Product product, MultipartFile multipartFile) {
//  -----------amazonS3client---------------
//        if (multipartFile == null) {
//            product.setFilename(amazonS3client.getUrl(bucketName, "empty.jpg").toString());
//        } else {
//            File file = new File(multipartFile.getOriginalFilename());
//            try (FileOutputStream fos = new FileOutputStream(file)) {
//                fos.write(multipartFile.getBytes());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            String fileName = UUID.randomUUID().toString() + "." + multipartFile.getOriginalFilename();
//            amazonS3client.putObject(new PutObjectRequest(bucketName, fileName, file));
//            product.setFilename(amazonS3client.getUrl(bucketName, fileName).toString());
//            file.delete();
//        }

//        if (multipartFile == null) {
//            product.setFilename("empty-image.png");
//        } else {
//
//            File uploadDir = null;
//            try {
//                uploadDir = new File(uploadPath.getFile().getAbsolutePath());
//                if (!uploadDir.exists()){
//                    uploadDir.mkdir();
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            File file = new File(uploadDir + "/" + multipartFile.getOriginalFilename());
//            try (FileOutputStream fos = new FileOutputStream(file)) {
//                fos.write(multipartFile.getBytes());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public String deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiRequestException(ErrorMessage.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
        productRepository.delete(product);
        return "Product deleted successfully";
    }

}
