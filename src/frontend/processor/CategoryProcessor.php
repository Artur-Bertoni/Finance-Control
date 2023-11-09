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
    $result = $service->delete($_SESSION['categoryId']);

    if ($result != null) {
        echo "<script>alert('" . $result . "');location.href=\"../Category.html\";</script>";
        exit;
    }

    header("Location: ../CategoryDashboard.html");
    exit;
}

$categoryId = $_SESSION['categoryId'];
$userId = $_SESSION['userId'];
$name = $_POST["nameField"];
$description = $_POST["descriptionField"];

if ($categoryId != "") {
    $result = $service->update($categoryId, new CategoryRequestDTO(
        $userId,
        $name,
        $description
    ));
} else {
    $result = $service->create(new CategoryRequestDTO(
        $userId,
        $name,
        $description
    ));
}

if (!$result instanceof Category) {
    echo "<script>alert('" . $result . "');location.href=\"../Category.html\";</script>";
    exit;
}

header("Location: ../CategoryDashboard.html");
exit;
