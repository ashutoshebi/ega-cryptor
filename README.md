# ega-cryptor
Enables submitters to produce EGA compliant files. Multiple files will be processed in parallel.

#How to run
Make sure Java 1.8 or above version is installed on your system. type **java -version** and press enter to
check what java version is installed. Download the **ega-cryptor-1.0.0.jar** and navigate to folder in which jar has been downloaded.

- ##### Single File To Encrypt
  It will generate output files at same location as that of source file.
  ```
  java -jar ega-cryptor-1.0.0.jar --fileToEncryptPaths="path/to/file/tobe/encrypted/file.bam"
  ```
  It will generate output files in the target directory specified in outputFolderPath.
  ```
  java -jar ega-cryptor-1.0.0.jar --fileToEncryptPaths="path/to/file/tobe/encrypted/file.bam" --outputFolderPath="path/to/output/folder"
  ```
- ##### Multiple Files or/and Folders To Encrypt
  Specify multiple files comma separated. It will generate output files at same location as that of source files.
  ```
  java -jar ega-cryptor-1.0.0.jar --fileToEncryptPaths="path/to/file/tobe/encrypted/file1.bam, path/to/file/tobe/encrypted/file2.bam, path/to/folder/tobe/encrypted/test1, path/to/folder/tobe/encrypted/test2"
  ```
  It will generate output files in the target directory specified in outputFolderPath.
  ```
  java -jar ega-cryptor-1.0.0.jar --fileToEncryptPaths="path/to/file/tobe/encrypted/file1.bam, path/to/folder/tobe/encrypted/test1" --outputFolderPath="path/to/output/folder"
  ```
