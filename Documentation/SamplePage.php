<html>
<head></head>
<body>
<?php

    include "../inc/dbinfo.inc";
    $con = mysqli_connect(DB_SERVER,DB_USERNAME,DB_PASSWORD);
    if(mysqli_connect_errno()) echo "<p>MySQL DB Connection Fail", mysqli_connect_error(),"</p>";


    $database = mysqli_select_db($con,DB_DATABASE);

    $resultSet = mysqli_query($con,$_POST["sqlQuery"]);

    while($qdata=mysqli_fetch_row($resultSet)){
        $whiteSpaceSeparated = implode(" ",$qdata);
        echo "<p>",$whiteSpaceSeparated,"</p>";
    }
?>
</body>
</html>