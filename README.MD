**Проект использует подгрузку ключей дефолтным способом.**

**На `Windows` нужно положить файл `credentials` с ключами в директорию `Имя_Пользовтеля/.aws`**

**На Unix-системах файл нужно положить В `$HOME` или в `~`**


**Пример файла credentials**

```
[default]
aws_access_key_id=AKIAIOSFODNN7EXAMPLE
aws_secret_access_key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

**Для сборкии можно использовать сборщик maven, со следующими параметрами: `mvn clean compile assembly:single`**

**Собранный файл получается большой, поэтому не поместился на гит. Загрузил его на [Яндекс.Диск]()**

**Запустить его можно с помощью команды `java -jar путь_до_файла/vvot-1.0-SNAPSHOT-jar-with-dependencies.jar cloudphoto аргументы(download, upload, list)`**



