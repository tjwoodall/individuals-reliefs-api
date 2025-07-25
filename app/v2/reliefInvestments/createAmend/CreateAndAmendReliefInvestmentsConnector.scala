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

package v2.reliefInvestments.createAmend

import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v2.reliefInvestments.createAmend.model.request.CreateAndAmendReliefInvestmentsRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendReliefInvestmentsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def amend(request: CreateAndAmendReliefInvestmentsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    lazy val downstreamUri1629: DownstreamUri[Unit] =
      IfsUri[Unit](s"income-tax/reliefs/investment/$nino/${taxYear.asMtd}")

    lazy val downstreamUri1924: DownstreamUri[Unit] =
      if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1924")) {
        HipUri(s"itsa/income-tax/v1/${taxYear.asTysDownstream}/reliefs/investment/$nino")
      } else {
        IfsUri[Unit](s"income-tax/reliefs/investment/${taxYear.asTysDownstream}/$nino")
      }

    val downstreamUri: DownstreamUri[Unit] = if (taxYear.useTaxYearSpecificApi) {
      downstreamUri1924
    } else {
      downstreamUri1629
    }

    put(body, downstreamUri)
  }

}
