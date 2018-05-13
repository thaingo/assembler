/*
 * Copyright 2018 Sebastien Pelletier
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

package io.github.pellse.assembler.flux;

import io.github.pellse.assembler.AssemblerTestUtils;
import io.github.pellse.assembler.AssemblerTestUtils.*;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static io.github.pellse.assembler.AssemblerTestUtils.*;
import static io.github.pellse.util.query.MapperUtils.oneToManyAsList;
import static io.github.pellse.util.query.MapperUtils.oneToOne;
import static java.util.Arrays.asList;

public class FluxAssemblerTest {

    private List<Customer> getCustomers() {
        return asList(customer1, customer2, customer3, customer1, customer2);
    }

    @Test
    public void testAssembleWithFlux() {

        StepVerifier.create(FluxAssembler.of(this::getCustomers, Customer::getCustomerId)
                .assemble(
                        oneToOne(AssemblerTestUtils::getBillingInfoForCustomers, BillingInfo::getCustomerId, BillingInfo::new),
                        oneToManyAsList(AssemblerTestUtils::getAllOrdersForCustomers, OrderItem::getCustomerId),
                        Transaction::new))
                .expectSubscription()
                .expectNext(transaction1, transaction2, transaction3, transaction1, transaction2)
                .expectComplete()
                .verify();
    }

    @Test
    public void testAssembleWithFluxWithBuffering() {

        StepVerifier.create(Flux.fromIterable(getCustomers())
                .buffer(3)
                .flatMap(customers -> FluxAssembler.of(customers, Customer::getCustomerId)
                        .assemble(
                                oneToOne(AssemblerTestUtils::getBillingInfoForCustomers, BillingInfo::getCustomerId, BillingInfo::new),
                                oneToManyAsList(AssemblerTestUtils::getAllOrdersForCustomers, OrderItem::getCustomerId),
                                Transaction::new)))
                .expectSubscription()
                .expectNext(transaction1, transaction2, transaction3, transaction1, transaction2)
                .expectComplete()
                .verify();
    }
}
