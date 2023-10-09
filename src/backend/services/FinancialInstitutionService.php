<?php

include_once "../../backend/repository/FinancialInstitutionRepository.php";
include_once "../../backend/dto/FinancialInstitutionRequestDTO.php";

class FinancialInstitutionService {
    public function create(FinancialInstitutionRequestDTO $requestDTO) {
        return save($requestDTO);
    }

    public function update($id, FinancialInstitutionRequestDTO $requestDTO) {
        return update($id, $requestDTO);
    }
}
