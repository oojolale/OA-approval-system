package com.OA.system.repository;

import com.OA.system.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    Optional<Company> findByCompanyName(String companyName);
    
    List<Company> findByCompanyType(String companyType);
    
    List<Company> findByCertificationStatus(String certificationStatus);
    
    Optional<Company> findByCertificationEmail(String certificationEmail);
}
