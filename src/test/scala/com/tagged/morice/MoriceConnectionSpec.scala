package com.tagged.morice

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Get, Put, HConnectionManager}
import org.specs2.mutable.Specification

/**
 * This test requires that a test table be pre-created on the HBase cluster:
 *
 *     create 'MoriceConnectionSpec', 'family1', 'family2'
 */
class MoriceConnectionSpec extends Specification {

  val tableName = "MoriceConnectionSpec"
  val family1 = "family1"
  val family2 = "family2"

  val configuration = HBaseConfiguration.create()
  scala.util.Properties.propOrNone("hbase.zookeeper.quorum") match {
    case Some(quorum) => configuration.set("hbase.zookeeper.quorum", quorum)
    case None => Unit
  }

  val connection = HConnectionManager.createConnection(configuration)
  val morice = new MoriceConnection(connection)

  "withTable" should {

    "let us write and read using HTableInterface primitives" in {
      val rowkey = System.nanoTime().toString.getBytes
      val qualifier = "primitiveReadWriteTest".getBytes
      val writeValue = System.nanoTime().toString.getBytes

      val readResult = morice.withTable(tableName) { table =>
        val put = new Put(rowkey)
        put.add(family1.getBytes, qualifier, writeValue)
        table.put(put)
        table.flushCommits()

        val get = new Get(rowkey)
        get.addColumn(family1.getBytes, qualifier)
        val result = table.get(get)
        result.getValue(family1.getBytes, qualifier)
      }

      readResult must beEqualTo(writeValue)
    }

    "let us write and read byte arrays by row key" in {
      val rowKey = System.nanoTime().toString.getBytes
      val column1 = Morice.ColumnFamily(family1).column("byteArrayReadWriteTest1")
      val column2 = Morice.ColumnFamily(family2).column("byteArrayReadWriteTest2")
      val value1 = System.nanoTime().toString.getBytes
      val value2 = System.nanoTime().toString.getBytes

      val insertMap = Map(column1 -> value1, column2 -> value2)
      morice.writeBytes(tableName, rowKey, insertMap)
      val readResult = morice.readBytes(tableName, rowKey, insertMap.keys)

      // converting the values from arrays to strings allows this equality to work
      readResult.mapValues({ v => new String(v) }) must beEqualTo(insertMap.mapValues({ v => new String(v) }))
    }

  }

}
