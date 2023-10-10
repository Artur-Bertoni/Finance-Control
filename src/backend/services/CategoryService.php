<?php

include_once "../../backend/repository/CategoryRepository.php";
include_once "../../backend/dto/CategoryRequestDTO.php";

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
}
