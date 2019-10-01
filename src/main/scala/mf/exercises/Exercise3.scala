package mf.exercises

import cats.data.ValidatedNel

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import mf.models._
import cats.implicits._
import mf.http.Http

/**
 * Should fail if any network call to Http.getbatch fails
 * Should fail if any BatchResponse.response element fails to parse
 * Should collect all parsing failures
 */
class Exercise3(implicit ec: ExecutionContext) {
  def requestBatch(requests: List[Request],
                   batchSize: Int): Future[ValidatedNel[ServiceError, List[ParsedResponse]]] = {
    val batches = requests.grouped(batchSize).toList.map(BatchRequest.apply)
    batches.traverse(getAndParseBatch).map(_.combineAll)
  }

  def getAndParseBatch(batch: BatchRequest): Future[ValidatedNel[ServiceError, List[ParsedResponse]]] =
    Http.getBatch(batch).map(parseResponse(_, ParsedResponse.parser))

  private def parseResponse(responses: BatchResponse,
                            parser: String => Try[ParsedResponse]): ValidatedNel[ServiceError, List[ParsedResponse]] =
    responses.responses.traverse(
      raw => parser(raw.value).toEither.leftMap(e => ServiceError(e.getMessage)).toValidatedNel
    )

}
