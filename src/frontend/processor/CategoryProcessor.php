<?php

include "../../backend/services/CategoryService.php";

session_start();

if (isset($_POST['cancelButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

$service = new CategoryService();

$categoryId = $_POST['categoryId'];
$userId = $_SESSION['userId'];
$name = $_POST["nameField"];
$description = $_POST["descriptionField"];

if ($categoryId != "") {
    $service->update($categoryId, new CategoryRequestDTO(
        $userId,
        $name,
        $description
    ));
} else {
    $service->create(new CategoryRequestDTO(
        $userId,
        $name,
        $description
    ));
}

header("Location: ../HomePage.html");
