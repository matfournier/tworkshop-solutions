package mf.exercises

import mf.models._
import monix.eval.Task
import cats.implicits._
import mf.http.Http

import scala.util.Try

class Exercise1Task {

  def requestBatch(requests: List[Request], batchSize: Int): Task[Either[ServiceError, List[ParsedResponse]]] = {
    val batches = requests.grouped(batchSize).toList.map(BatchRequest.apply)
    batches.traverse(getAndParse).map(_.combineAll)
  }

  private def getAndParse(batchRequest: BatchRequest): Task[Either[ServiceError, List[ParsedResponse]]] =
    Http.getBatchTask(batchRequest).map(batchResponse => parseResponse(batchResponse, ParsedResponse.parser))

  private def parseResponse(responses: BatchResponse,
                            parser: String => Try[ParsedResponse]): Either[ServiceError, List[ParsedResponse]] =
    responses.responses.traverse(raw => parser(raw.value).toEither.leftMap(e => ServiceError(e.getMessage)))

}
