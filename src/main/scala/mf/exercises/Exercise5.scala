package mf.exercises

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import mf.models._
import cats.implicits._
import mf.http.Http

import scala.util.control.NonFatal

/**
  * Should not stop for any Http.getBatch failures
  * Should fail if any BatchResponse.response element fails to parse
  * Should collect all parsing failures and all network failures
  */
class Exercise5(implicit ec: ExecutionContext) {
  def requestBatch(requests: List[Request], batchSize: Int): Future[(List[ServiceError], List[ParsedResponse])] = {
    val batches = requests.grouped(batchSize).toList.map(BatchRequest.apply)
    batches.traverse(getAndParseBatch).map(_.combineAll)
  }

  def getAndParseBatch(batch: BatchRequest): Future[(List[ServiceError], List[ParsedResponse])] =
    Http.getBatch(batch).map(parseResponse(_, ParsedResponse.parser)).recoverWith {
      case NonFatal(e) => Future.successful( (ServiceError(e.getMessage)::Nil, List.empty[ParsedResponse]))
    }

  private def parseResponse(responses: BatchResponse,
                            parser: String => Try[ParsedResponse]): (List[ServiceError], List[ParsedResponse]) = {
    responses.responses.map(raw => parser(raw.value).toEither match {
      case Left(e) => (ServiceError(e.getMessage) :: Nil, Nil)
      case Right(pr) => (Nil, pr :: Nil)
    }).combineAll
  }
}

