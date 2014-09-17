package com.tagged.pho.filter

import org.apache.hadoop.hbase.filter.{FilterList, Filter}

case class OrFilter(filters: PhoFilter*) extends PhoFilter {

  lazy val getFilter: Filter = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters.map(_.getFilter): _*)

}
