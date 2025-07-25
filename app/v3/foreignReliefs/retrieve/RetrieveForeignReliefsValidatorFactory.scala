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

package v3.foreignReliefs.retrieve

import shared.controllers.validators.Validator
import v3.foreignReliefs.retrieve.def1.model.response.Def1_RetrieveForeignReliefsValidator
import v3.foreignReliefs.retrieve.model.request.RetrieveForeignReliefsRequestData

import javax.inject.Singleton

@Singleton
class RetrieveForeignReliefsValidatorFactory {

  def validator(nino: String, taxYear: String): Validator[RetrieveForeignReliefsRequestData] = {
    taxYear match {
      case _ => new Def1_RetrieveForeignReliefsValidator(nino, taxYear)

    }

  }

}
