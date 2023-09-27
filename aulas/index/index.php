<?php
$nome = 'Artur <br>';
echo $nome;

$intUm = 100;
$intDois = 10.1;

echo $resultado = $intUm + $intDois . '<br>';
echo $resultado = $intUm * $intDois . '<br>';
echo $resultado = ($intUm += $intUm) . '<br>';
echo $resultado = $intUm++ . '<br><br>';

if (1 == 1 and 12 == 12 or 2 == 2 xor 3 == 3) {
    echo 'true';
}

function calcula($valor1, $valor2) {
    echo "<br><br>Resultado: " . ($valor1 + $valor2) . "<br><br>";
}

calcula(10, 5);

function escopo($nome) {
//    echo "Escopo";
    $variavel = 2;
    return $variavel . "<br>" . $nome;
}

$ola = escopo("ENHOUT") . "<br><br>";
echo $ola;

function calculator($num1, $num2, $operation) {
    switch ($operation) {
        case '+':
            return $num1 + $num2;
        case '-':
            return $num1 - $num2;
        case '*':
            return $num1 * $num2;
        case '/':
            return ($num1 / $num2) . '<br>Resto: ' . ($num1 % $num2);
        default:
            return 'Erro';
    }
}

echo calculator(1, 2, '+') . "<br><br>";


function tabuada($num) {
    echo "Tabuada de " . $num . ":<br>";

    $i = 1;
    while($i <= 10) {
        echo ($num * $i) . "<br>";
        $i++;
    }
}

tabuada(5);

// Vetor Unidimensional

for ($i = 0; $i < 3; $i++)
    $vetor[$i] = "Valor " . $i . "<br>";

echo "<br><br>Vetor:<br>";

for ($i = 0; $i < 3; $i++)
    echo $vetor[$i];

// Vetor Bidimencional

for ($i = 0; $i < 3; $i++)
    for ($l = 0; $l < 3; $l++)
        $vetor2[$i][$l] = "L".$i."C".$l."  ";

echo "<br><br>Vetor Bidimensional:<br>";

for ($i = 0; $i < 3; $i++) {
    for ($l = 0; $l < 3; $l++)
        echo $vetor2[$i][$l];
    echo "<br>";
}

echo '<br><br>';

foreach ($vetor as $item)
    echo $item;

