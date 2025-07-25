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

package v3.charitableGiving.retrieve

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v3.charitableGiving.retrieve.def1.model.response.{Def1_GiftAidPayments, Def1_Gifts, Def1_NonUkCharities}
import v3.charitableGiving.retrieve.model.request.Def1_RetrieveCharitableGivingReliefsRequestData
import v3.charitableGiving.retrieve.model.response.Def1_RetrieveCharitableGivingReliefsResponse

import scala.concurrent.Future

class RetrieveCharitableGivingReliefsServiceSpec extends ServiceSpec {

  private val nino    = "ZG903729C"
  private val taxYear = "2017-18"

  private val request = Def1_RetrieveCharitableGivingReliefsRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val nonUkCharitiesModel = Def1_NonUkCharities(
    charityNames = Some(Seq("non-UK charity 1", "non-UK charity 2")),
    totalAmount = 1000.10
  )

  private val giftAidPaymentsModel = Def1_GiftAidPayments(
    nonUkCharities = Some(nonUkCharitiesModel),
    totalAmount = Some(1000.11),
    oneOffAmount = Some(1000.12),
    amountTreatedAsPreviousTaxYear = Some(1000.13),
    amountTreatedAsSpecifiedTaxYear = Some(1000.14)
  )

  private val giftsModel = Def1_Gifts(
    nonUkCharities = Some(nonUkCharitiesModel),
    landAndBuildings = Some(1000.15),
    sharesOrSecurities = Some(1000.16)
  )

  private val response = Def1_RetrieveCharitableGivingReliefsResponse(
    giftAidPayments = Some(giftAidPaymentsModel),
    gifts = Some(giftsModel)
  )

  trait Test extends MockRetrieveCharitableGivingReliefsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveCharitableGivingReliefsService(
      connector = mockConnector
    )

  }

  "RetrieveCharitableGivingReliefService" when {
    "service call successful" should {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, response))

        MockRetrieveCharitableGivingReliefsConnector
          .retrieve(request)
          .returns(Future.successful(outcome))

        await(service.retrieve(request)) shouldBe outcome
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrieveCharitableGivingReliefsConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.retrieve(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TYPE", InternalError),
          ("INVALID_TAXYEAR", TaxYearFormatError),
          ("INVALID_INCOME_SOURCE", InternalError),
          ("NOT_FOUND_PERIOD", NotFoundError),
          ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = Seq(
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATION_ID", InternalError),
          ("INVALID_INCOMESOURCE_ID", InternalError),
          ("INVALID_INCOMESOURCE_TYPE", InternalError),
          ("SUBMISSION_PERIOD_NOT_FOUND", NotFoundError),
          ("INCOME_DATA_SOURCE_NOT_FOUND", NotFoundError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )
        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
