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

package v3.reliefInvestments.createAmend.def1.model.request

import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v3.reliefInvestments.createAmend.def1.model.Def1_CreateAndAmendReliefInvestmentsFixtures._

class Def1_CreateAndAmendReliefInvestmentsRequestBodySpec extends UnitSpec with JsonErrorValidators {

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        Def1_requestBodyJson.as[Def1_CreateAndAmendReliefInvestmentsRequestBody] shouldBe Def1_requestBodyModel
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Def1_requestBodyModel.toJson shouldBe Def1_requestBodyJson
      }
    }
  }

  "isIncorrectOrEmptyBodyError" should {
    "return false" when {
      "all arrays are provided, none are empty, no objects in the arrays are empty" in {
        Def1_requestBodyModel.isIncorrectOrEmptyBody shouldBe false
      }

      "only some arrays are provided, none are empty, no objects in the arrays are empty" in {
        val model = Def1_requestBodyModel.copy(
          vctSubscription = None,
          seedEnterpriseInvestment = None
        )
        model.isIncorrectOrEmptyBody shouldBe false
      }
    }

    "return true" when {
      "no arrays are provided" in {
        val model = Def1_CreateAndAmendReliefInvestmentsRequestBody(
          None,
          None,
          None,
          None,
          None
        )
        model.isIncorrectOrEmptyBody shouldBe true
      }
      "at least one empty array is provided" in {
        val model = Def1_requestBodyModel.copy(
          vctSubscription = Some(Seq()),
          communityInvestment = Some(Seq())
        )
        model.isIncorrectOrEmptyBody shouldBe true
      }
    }
  }

}
