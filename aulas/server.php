<?php

echo $_SERVER['SERVER_PORT'];

echo "<br>";
foreach($_SERVER as $key_name => $key_value)
{
    print $key_name . " = " . $key_value . "<br>";
}
