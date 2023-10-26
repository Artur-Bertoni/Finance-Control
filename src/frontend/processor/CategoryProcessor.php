<?php

include "../../backend/services/CategoryService.php";

session_start();

$service = new CategoryService();

if (isset($_POST['homeButton']) || isset($_POST['cancelButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}

$categoryId = $_SESSION['categoryId'];
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
