package mf.exercises

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import mf.models._
import cats.implicits._
import mf.http.Http

/**
 * Should fail if any call to Http.getBatch fails
 * Should fail if any BatchResponse.response element fails to parse into a ParsedResponse using the
 * ParsedResponse.parser
 */
class Exercise1(implicit ec: ExecutionContext) {
  def requestBatch(requests: List[Request], batchSize: Int): Future[Either[ServiceError, List[ParsedResponse]]] = {
    val batches = requests.grouped(batchSize).toList.map(BatchRequest.apply)
    batches.traverse(getAndParseBatch).map(_.combineAll)
  }

  def getAndParseBatch(batch: BatchRequest): Future[Either[ServiceError, List[ParsedResponse]]] =
    Http.getBatch(batch).map(parseResponse(_, ParsedResponse.parser))

  private def parseResponse(responses: BatchResponse,
                            parser: String => Try[ParsedResponse]): Either[ServiceError, List[ParsedResponse]] =
    responses.responses.traverse(raw => parser(raw.value).toEither.leftMap(e => ServiceError(e.getMessage)))

}
