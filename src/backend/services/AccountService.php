<?php

include_once "../../backend/repository/AccountRepository.php";
include_once "../../backend/repository/FinancialInstitutionRepository.php";
include_once "../../backend/dto/AccountRequestDTO.php";
include_once "../../backend/dto/AccountDTO.php";

global $repository, $financialInstitutionRepository;
$repository = new AccountRepository();
$financialInstitutionRepository = new FinancialInstitutionRepository();

class AccountService
{
    public function create(AccountRequestDTO $requestDTO)
    {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, AccountRequestDTO $requestDTO)
    {
        global $repository;
        return $repository->update($id, $requestDTO);
    }

    public function findAllByUser($userId): void
    {
        global $repository;
        $accounts = $repository->findAllByUserId($userId);
        $accountDTOs = [];

        foreach ($accounts as $account) {
            $accountDTOs[] = $this->buildAccountDTO($account);
        }

        echo json_encode($accountDTOs);
    }

    private function buildAccountDTO($account): AccountDTO
    {
        global $financialInstitutionRepository;

        return new AccountDTO(
            $account->getId(),
            $financialInstitutionRepository->findById($account->getFinancialInstitutionId()),
            $account->getName(),
            $account->getContact(),
            $account->getDescription()
        );
    }

    public function findById($id): void
    {
        global $repository;
        $account = $repository->findById($id);
        echo json_encode($this->buildAccountDTO($account));
    }

    public function delete($id): void
    {
        global $repository;
        $repository->delete($id);
    }
}
