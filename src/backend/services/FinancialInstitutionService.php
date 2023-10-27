<?php

include_once "../../backend/repository/FinancialInstitutionRepository.php";
include_once "../../backend/dto/FinancialInstitutionRequestDTO.php";
include_once "../../backend/dto/FinancialInstitutionDTO.php";

global $repository;
$repository = new FinancialInstitutionRepository();

class FinancialInstitutionService
{
    public function create(FinancialInstitutionRequestDTO $requestDTO): FinancialInstitution|string
    {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, FinancialInstitutionRequestDTO $requestDTO): FinancialInstitution|string
    {
        global $repository;
        return $repository->update($id, $requestDTO);
    }

    public function findAllByUser($userId): void
    {
        global $repository;
        $financialInstitutions = $repository->findAllByUserId($userId);
        $financialInstitutionDTOs = [];

        foreach ($financialInstitutions as $financialInstitution)
            $financialInstitutionDTOs[] = $this->buildFinancialInstitutionDTO($financialInstitution);

        echo json_encode($financialInstitutionDTOs);
    }

    public function findById($id): void
    {
        global $repository;
        $financialInstitution = $repository->findById($id);
        echo json_encode($this->buildFinancialInstitutionDTO($financialInstitution));
    }

    public function delete($id): ?string
    {
        global $repository;
        return $repository->delete($id);
    }

    private function buildFinancialInstitutionDTO($financialInstitution): FinancialInstitutionDTO
    {
        return new FinancialInstitutionDTO(
            $financialInstitution->getId(),
            $financialInstitution->getName(),
            $financialInstitution->getAddress(),
            $financialInstitution->getContact()
        );
    }
}
