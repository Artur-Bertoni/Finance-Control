<?php

include_once "../../backend/repository/FinancialInstitutionRepository.php";
include_once "../../backend/dto/FinancialInstitutionRequestDTO.php";

global $repository;
$repository = new FinancialInstitutionRepository();
class FinancialInstitutionService {
    public function create(FinancialInstitutionRequestDTO $requestDTO) {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, FinancialInstitutionRequestDTO $requestDTO) {
        global $repository;
        return $repository->update($id, $requestDTO);
    }
}
