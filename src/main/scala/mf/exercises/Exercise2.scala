package mf.exercises

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import mf.models._
import cats.implicits._
import mf.http.Http

class Exercise2(implicit ec: ExecutionContext) {
  def requestBatch(requests: List[Request], batchSize: Int): Future[List[ParsedResponse]] = {
    val batches = requests.grouped(batchSize).toList.map(BatchRequest.apply)
    batches.traverse(getAndParseBatch).map(_.combineAll)
  }

  private def getAndParseBatch(batch: BatchRequest): Future[List[ParsedResponse]] =
    Http.getBatch(batch).map(parseResponse(_, ParsedResponse.parser))

  private def parseResponse(responses: BatchResponse,
                            parser: String => Try[ParsedResponse]): List[ParsedResponse] =
    responses.responses.flatMap(raw => parser(raw.value).toOption)

}

