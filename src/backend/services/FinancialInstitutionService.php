<?php

include_once "../../backend/repository/FinancialInstitutionRepository.php";
include_once "../../backend/dto/FinancialInstitutionRequestDTO.php";
include_once "../../backend/dto/FinancialInstitutionDTO.php";

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

    public function findAllByUser($userId) {
        global $repository;
        $financialInstitutions = $repository->findAllByUserId($userId);
        $financialInstitutionDTOs = [];

        foreach ($financialInstitutions as $financialInstitution) {
            $financialInstitutionDTOs[] = new FinancialInstitutionDTO(
                $financialInstitution->getId(),
                $financialInstitution->getName(),
                $financialInstitution->getAddress(),
                $financialInstitution->getContact()
            );
        }

        echo json_encode($financialInstitutionDTOs);
    }
}
