<?php

include "../../backend/services/CategoryService.php";

session_start();

$service = new CategoryService();

if (isset($_POST['homeButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['cancelButton'])) {
    header("Location: ../CategoryDashboard.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}

if (isset($_POST['deleteButton'])) {
    $service->delete($_SESSION['categoryId']);
    header("Location: ../CategoryDashboard.html");
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

header("Location: ../CategoryDashboard.html");
