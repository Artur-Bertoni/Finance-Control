<?php

include_once "../../backend/repository/CategoryRepository.php";
include_once "../../backend/dto/CategoryRequestDTO.php";
include_once "../../backend/dto/CategoryDTO.php";

global $repository;
$repository = new CategoryRepository();
class CategoryService {
    public function create(CategoryRequestDTO $requestDTO) {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, CategoryRequestDTO $requestDTO) {
        global $repository;
        return $repository->update($id, $requestDTO);
    }

    public function findAllByUser($userId) {
        global $repository;
        $categories = $repository->findAllByUserId($userId);
        $categoryDTOs = [];

        foreach ($categories as $category) {
            $categoryDTOs[] = new CategoryDTO(
                $category->getId(),
                $category->getName(),
                $category->getDescription()
            );
        }

        echo json_encode($categoryDTOs);
    }
}
