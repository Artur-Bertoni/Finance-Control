<?php

include_once "../../backend/repository/CategoryRepository.php";
include_once "../../backend/dto/CategoryRequestDTO.php";

class CategoryService {
    public function create(CategoryRequestDTO $requestDTO) {
        return save($requestDTO);
    }

    public function update($id, CategoryRequestDTO $requestDTO) {
        return update($id, $requestDTO);
    }
}
