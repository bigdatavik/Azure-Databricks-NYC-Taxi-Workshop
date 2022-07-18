// Databricks notebook source
// MAGIC %md
// MAGIC # ADLS gen 2 - primer
// MAGIC Azure Data Lake Storage Gen2 combines the capabilities of two existing storage services: Azure Data Lake Storage Gen1 features, such as file system semantics, file-level security and scale are combined with low-cost, tiered storage, high availability/disaster recovery capabilities, and a large SDK/tooling ecosystem from Azure Blob Storage.<br><br>
// MAGIC 
// MAGIC ### What's in this exercise?
// MAGIC We will complete the following in batch operations on DBFS-Hierarchical Name Space enabled ADLS Gen2:<br>
// MAGIC 1.  Set credentials for access<br>
// MAGIC 2.  Create the root file system<br>
// MAGIC 3.  Create a couple directories, list directories, delete a directory<br>
// MAGIC 4.  Create a dataset and persist to ADLS Gen2, create external table and run queries<br>
// MAGIC 5.  Read data from ADLS Gen2<br>
// MAGIC 
// MAGIC Not supported currently but on the roadmap:<br> 
// MAGIC 1.  Mounting ADLS Gen2 
// MAGIC 2.  SAS tokens 
// MAGIC 
// MAGIC References:<br>
// MAGIC ADLS Gen2 product page:https://docs.microsoft.com/en-us/azure/storage/data-lake-storage/using-databricks-spark<br>
// MAGIC Databricks ADLS Gen2 integration: https://docs.azuredatabricks.net/spark/latest/data-sources/azure/azure-datalake-gen2.html

// COMMAND ----------

// MAGIC %md
// MAGIC # I am trying out mounting at the moment (brand new feature) - so this notebook will not execute flawlessly

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1.0. Credentials setting

// COMMAND ----------

//vik
val azSubcriptionId = dbutils.secrets.get(scope = "common-sp", key = "az-sub-id")
val azTenantId = dbutils.secrets.get(scope = "common-sp", key = "az-tenant-id")
val spId = dbutils.secrets.get(scope = "common-sp", key = "common-sa-sp-client-id")
val spSecret = dbutils.secrets.get(scope = "common-sp", key = "common-sa-sp-client-secret")
val sp2Id = dbutils.secrets.get(scope = "common-sp", key = "common-sa-sp2-client-id")
val sp2Secret = dbutils.secrets.get(scope = "common-sp", key = "common-sa-sp2-client-secret")


// COMMAND ----------

// val configs = Map(
//   "fs.azure.account.auth.type" -> "OAuth",
//   "fs.azure.account.oauth.provider.type" -> "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider",
//   "fs.azure.account.oauth2.client.id" -> (dbutils.secrets.get(scope = "gws-adlsgen2-storage", key = "client-id")), //App ID
//   "fs.azure.account.oauth2.client.secret" -> (dbutils.secrets.get(scope = "gws-adlsgen2-storage", key = "client-secret")), //App key
//   "fs.azure.account.oauth2.client.endpoint" -> ()"https://login.microsoftonline.com/" + dbutils.secrets.get(scope = "gws-adlsgen2-storage", key = "client-secret") + "/oauth2/token") //AAD Tenant ID
// )

val tenantID = "https://login.microsoftonline.com/" + azTenantId + "/oauth2/token"
val adlsConfigs = Map("fs.azure.account.auth.type" -> "OAuth",
  "fs.azure.account.oauth.provider.type" -> "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider",
  "fs.azure.account.oauth2.client.id" -> spId,
  "fs.azure.account.oauth2.client.secret" -> spSecret,
  "fs.azure.account.oauth2.client.endpoint" -> tenantID)

// COMMAND ----------

// NOT REQUIRED if you are mounting
// This is for when you want to use storage account key-based access
// Replace gwsadlsgen2sa with your ADLS Gen2 Account name
// val adlsgen2acct = "gwsadlsgen2sa"
// val adlsgen2key = dbutils.secrets.get(scope = "gws-adlsgen2-storage", key = "storage-acct-key")
//  "spark.hadoop.fs.azure.account.key.dltdemostorage.dfs.core.windows.net": "{{secrets/access_creds/adlsDltDemoStorageAccessKey}}"

val adlsgen2acct = "vm186007"
val adlsgen2key = dbutils.secrets.get(scope = "access_creds_vkm", key = "adlsDltDemoStorageAccessKey")
spark.conf.set("fs.azure.account.key." + adlsgen2acct + ".dfs.core.windows.net", adlsgen2key) 


// COMMAND ----------

//Allow root filesystem creation
spark.conf.set("fs.azure.createRemoteFileSystemDuringInitialization", "true")

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2.0. Create root file system for each information zone

// COMMAND ----------

// MAGIC %md
// MAGIC The command below will create a root system-<br>
// MAGIC dbutils.fs.ls("abfss://<New_Root_Filesystem_Name>@gwsadlsgen2sa.dfs.core.windows.net/")<br>

// COMMAND ----------

dbutils.fs.ls("abfss://staging@vm186007.dfs.core.windows.net/")

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3.0. Mount ADLS Gen2 as file system

// COMMAND ----------

// dbutils.fs.mount(source = "abfss://staging@gwsadlsgen2sa.dfs.core.windows.net/", mountPoint = "/mnt/adlsgen2sa/staging/", extraConfigs = configs)

dbutils.fs.mount(
  source = "abfss://files@vm186007.dfs.core.windows.net/",
  mountPoint = "/mnt/vm186007/files",
  extraConfigs = adlsConfigs)

// COMMAND ----------

// MAGIC %md
// MAGIC ls /mnt/adlsgen2sa/staging

// COMMAND ----------

// MAGIC %fs
// MAGIC ls /mnt/vm186007/files

// COMMAND ----------

// MAGIC %md
// MAGIC mkdir /tmp
// MAGIC wget -P /tmp "https://generalworkshopsa.blob.core.windows.net/demo/If-By-Kipling.txt"

// COMMAND ----------

// MAGIC %md
// MAGIC wget -P /tmp "https://generalworkshopsa.blob.core.windows.net/demo/If-By-Kipling.txt"

// COMMAND ----------

// MAGIC %sh
// MAGIC wget -P /tmp "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"

// COMMAND ----------

// MAGIC %md
// MAGIC cat  /tmp/If-By-Kipling.txt

// COMMAND ----------

// MAGIC %sh
// MAGIC cat  /tmp/time_series_covid19_confirmed_global.csv

// COMMAND ----------

// MAGIC %md
// MAGIC cp file:/tmp/If-By-Kipling.txt /mnt/adlsgen2sa/staging/

// COMMAND ----------

// MAGIC %fs
// MAGIC cp file:/tmp/time_series_covid19_confirmed_global.csv /mnt/vm186007/files/

// COMMAND ----------

// MAGIC %md
// MAGIC ls /mnt/adlsgen2sa/staging

// COMMAND ----------

// MAGIC %fs
// MAGIC ls /mnt/vm186007/files/

// COMMAND ----------

// MAGIC %fs
// MAGIC head /mnt/adlsgen2sa/staging/If-By-Kipling.txt

// COMMAND ----------

// MAGIC %fs
// MAGIC head /mnt/vm186007/files/time_series_covid19_confirmed_global.csv

// COMMAND ----------

// MAGIC %md
// MAGIC ### 4.0. Create dataset

// COMMAND ----------

val booksDF = Seq(
   ("b00001", "Arthur Conan Doyle", "A study in scarlet", 1887),
   ("b00023", "Arthur Conan Doyle", "A sign of four", 1890),
   ("b01001", "Arthur Conan Doyle", "The adventures of Sherlock Holmes", 1892),
   ("b00501", "Arthur Conan Doyle", "The memoirs of Sherlock Holmes", 1893),
   ("b00300", "Arthur Conan Doyle", "The hounds of Baskerville", 1901)
).toDF("book_id", "book_author", "book_name", "book_pub_year")

booksDF.printSchema
booksDF.show

// COMMAND ----------

// MAGIC %md
// MAGIC ### 5.0. Persist in Delta format to DBFS

// COMMAND ----------

//Destination directory for Delta table
//val deltaTableDirectory = "abfss://scratch@gwsadlsgen2sa.dfs.core.windows.net/books"

//val deltaTableDirectory = "/mnt/adlsgen2sa/staging/books"
val deltaTableDirectory = "/mnt/vm186007/files/staging/books"
dbutils.fs.rm(deltaTableDirectory, recurse=true)

// COMMAND ----------

//Persist dataframe to delta format without coalescing
booksDF.write.format("delta").save(deltaTableDirectory)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 6.0. Create external table

// COMMAND ----------

// MAGIC %md
// MAGIC CREATE DATABASE IF NOT EXISTS books_db_adlsgen2;
// MAGIC 
// MAGIC USE books_db_adlsgen2;
// MAGIC DROP TABLE IF EXISTS books;
// MAGIC CREATE TABLE books
// MAGIC USING DELTA
// MAGIC LOCATION "/mnt/adlsgen2sa/staging/books/";

// COMMAND ----------

// MAGIC %sql
// MAGIC CREATE DATABASE IF NOT EXISTS books_db_adlsgen2;
// MAGIC 
// MAGIC USE books_db_adlsgen2;
// MAGIC DROP TABLE IF EXISTS books;
// MAGIC CREATE TABLE books
// MAGIC USING DELTA
// MAGIC LOCATION "/mnt/vm186007/files/staging/books/";

// COMMAND ----------



// COMMAND ----------

// MAGIC %md
// MAGIC ### 7.0. Query your table with SQL

// COMMAND ----------

// MAGIC %sql
// MAGIC select * from books_db_adlsgen2.books;

// COMMAND ----------

// MAGIC %md
// MAGIC dbutils.fs.ls("/mnt/adlsgen2sa/staging/books")

// COMMAND ----------

dbutils.fs.ls("/mnt/vm186007/files/staging/books")

// COMMAND ----------


