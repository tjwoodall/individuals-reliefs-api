/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v2.foreignReliefs.delete

import common.RuleOutsideAmendmentWindowError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.foreignReliefs.delete.model.Def1_DeleteForeignReliefsRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteForeignReliefsServiceSpec extends UnitSpec {

  val nino: String    = "ZG903729C"
  val taxYear: String = "2019-20"

  implicit val correlationId: String = "X-123"

  val requestData: Def1_DeleteForeignReliefsRequestData = Def1_DeleteForeignReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  trait Test extends MockDeleteForeignReliefsConnector {

    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeleteForeignReliefsService(connector = mockDeleteForeignReliefsConnector)

  }

  "service" when {

    "a service call is successful" should {

      "return a mapped result" in new Test {

        MockDeleteForeignReliefsConnector
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.delete(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }

    "a service call is unsuccessful" should {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} error when $downstreamErrorCode error is returned from the connector" in new Test {

          MockDeleteForeignReliefsConnector
            .delete(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.delete(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
        }

      val errors = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "NO_DATA_FOUND"             -> NotFoundError,
        "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError
      )

      val extraTysErrors = Seq(
        "INVALID_CORRELATION_ID" -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
