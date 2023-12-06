<?php

include "../../backend/services/FinancialInstitutionService.php";

session_start();

$service = new FinancialInstitutionService();

if (isset($_POST['homeButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['cancelButton'])) {
    header("Location: ../FinancialInstitutionDashboard.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}

if (isset($_POST['deleteButton'])) {
    $result = $service->delete($_SESSION['financialInstitutionId']);

    if ($result != null) {
        echo "<script>alert('" . $result . "');location.href=\"../FinancialInstitution.html\";</script>";
        exit;
    }

    header("Location: ../FinancialInstitutionDashboard.html");
    exit;
}

$financialInstitutionId = $_SESSION['financialInstitutionId'];
$userId = $_SESSION['userId'];
$name = $_POST["nameField"];
$address = $_POST["addressField"];
$contact = $_POST["contactField"];

if ($financialInstitutionId != "") {
    $result = $service->update($financialInstitutionId, new FinancialInstitutionRequestDTO(
        $userId,
        $name,
        $address,
        $contact
    ));
} else {
    $result = $service->create(new FinancialInstitutionRequestDTO(
        $userId,
        $name,
        $address,
        $contact
    ));
}

if (!$result instanceof FinancialInstitution) {
    echo "<script>alert('" . $result . "');location.href=\"../FinancialInstitution.html\";</script>";
    exit;
}

header("Location: ../FinancialInstitutionDashboard.html");
exit;
