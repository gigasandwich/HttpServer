<?php 
    $name = "Jean";
    $array = array("Fako", "Salama e");
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fichier php</title>
</head>
<body>
    <p><?php var_dump($array) echo jwadawd ?></p>

    <h2>Get request /folder1/treatment.php</h2>
    <form method="GET" action="/php.php">
        <label for="">Name:</label>
        <input type="text" id="" name="name">

        <label for="">Age:</label>
        <input type="text" id="" name="age">

        <button type="submit">Submit POST</button>          
    </form>

    <!-- POST Form mankany @ folder1 -->
    <h2>Post request /folder1/treatment.php</h2>
    <form method="POST" action="/php.php">
        <label for="">Name:</label>
        <input type="text" id="" name="name">

        <label for="">Age:</label>
        <input type="text" id="" name="age">

        <button type="submit">Submit POST</button>          
    </form>
</body>
</html>