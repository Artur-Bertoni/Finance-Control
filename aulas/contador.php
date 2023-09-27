<?php
session_name('meucontador');
session_start();

if(isset($_SESSION['contador'])){
     $_SESSION['contador']++;
}else{
     $_SESSION['contador']= 1;
}

echo $_SESSION['contador'];
