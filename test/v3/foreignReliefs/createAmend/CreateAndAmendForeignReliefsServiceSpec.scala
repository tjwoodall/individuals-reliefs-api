/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.foreignReliefs.createAmend

import common.RuleOutsideAmendmentWindowError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v3.foreignReliefs.createAmend.CreateAndAmendForeignReliefsFixtures.requestBodyModel
import v3.foreignReliefs.createAmend.def1.model.request.Def1_CreateAndAmendForeignReliefsRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAndAmendForeignReliefsServiceSpec extends UnitSpec {

  private val taxYear                        = "2021-22"
  private val nino                           = Nino("AA123456A")
  private implicit val correlationId: String = "X-123"

  private val requestData = Def1_CreateAndAmendForeignReliefsRequestData(nino, TaxYear.fromMtd(taxYear), requestBodyModel)

  trait Test extends MockCreateAndAmendForeignReliefsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateAndAmendForeignReliefsService(
      connector = mockCreateAndAmendForeignReliefsConnector
    )

  }

  "service" when {
    "service call successful" must {
      "return mapped result" in new Test {
        MockCreateAndAmendForeignReliefsConnector
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.createAndAmend(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockCreateAndAmendForeignReliefsConnector
            .createAndAmend(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.createAndAmend(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError,
        "INVALID_PAYLOAD"           -> InternalError,
        "INVALID_CORRELATIONID"     -> InternalError,
        "UNPROCESSABLE_ENTITY"      -> InternalError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError
      )

      val extraTysErrors = List(
        "INVALID_CORRELATION_ID" -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
