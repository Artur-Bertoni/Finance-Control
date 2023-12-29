<?php
session_start();
echo session_id();
echo "<br>";
echo "Nome =".$_SESSION['nome'];
echo "<br>";
echo "Idade =".$_SESSION['idade'];
echo "<br>";
echo "Pass =".$_SESSION['password'];



//print_r($_SESSION);
