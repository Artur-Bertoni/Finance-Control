<?php

include_once "../../backend/repository/CategoryRepository.php";
include_once "../../backend/dto/CategoryRequestDTO.php";
include_once "../../backend/dto/CategoryDTO.php";

global $repository;
$repository = new CategoryRepository();

class CategoryService
{
    public function create(CategoryRequestDTO $requestDTO)
    {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, CategoryRequestDTO $requestDTO)
    {
        global $repository;
        return $repository->update($id, $requestDTO);
    }

    public function findAllByUser($userId): void
    {
        global $repository;
        $categories = $repository->findAllByUserId($userId);
        $categoryDTOs = [];

        foreach ($categories as $category) {
            $categoryDTOs[] = $this->buildCategoryDTO($category);
        }

        echo json_encode($categoryDTOs);
    }

    public function findById($id): void
    {
        global $repository;
        $category = $repository->findById($id);
        echo json_encode($this->buildCategoryDTO($category));
    }

    private function buildCategoryDTO($category): CategoryDTO
    {
        return new CategoryDTO(
            $category->getId(),
            $category->getName(),
            $category->getDescription()
        );
    }

    public function delete($id): void
    {
        global $repository;
        $repository->delete($id);
    }
}
