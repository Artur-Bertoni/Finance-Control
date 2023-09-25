<?php

header("Location: ../HomePage.html");

if (array_key_exists('cancelButton', $_POST)) {
    header("Location: ../Login.html");
}
