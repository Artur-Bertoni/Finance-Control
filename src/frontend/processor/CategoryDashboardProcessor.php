<?php

include "../../backend/services/CategoryService.php";

session_start();

$service = new CategoryService();

if (array_key_exists('postCategory', $_POST)) {
    $_SESSION['categoryId'] = "";
    header("Location: ../Category.html");
    exit;
}

if (array_key_exists('itemButton', $_POST)) {
    $_SESSION['categoryId'] = $_POST['itemButton'];
    header("Location: ../Category.html");
    exit;
}

if (isset($_POST['homeButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}
