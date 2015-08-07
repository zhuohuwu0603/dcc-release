/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.etl2.job.join.core;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.model.FieldNames.LoaderFieldNames.AVAILABLE_RAW_SEQUENCE_DATA;
import static org.icgc.dcc.common.core.model.FieldNames.SubmissionFieldNames.SUBMISSION_ANALYZED_SAMPLE_ID;
import static org.icgc.dcc.etl2.core.util.ObjectNodes.textValue;

import java.io.File;
import java.util.List;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.etl2.core.job.FileType;
import org.icgc.dcc.etl2.core.util.ObjectNodes;
import org.icgc.dcc.etl2.test.job.AbstractJobTest;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;

@Slf4j
public class JoinJobTest extends AbstractJobTest {

  private static final ImmutableList<String> VALID_CONSEQUENCE_FIELDS = ImmutableList.of(
      "_gene_id",
      "_transcript_id",
      "aa_change",
      "cds_change",
      "consequence_type",
      "gene_affected",
      "gene_build_version",
      "note",
      "protein_domain_affected",
      "transcript_affected");

  private static final ImmutableList<String> COMMON_OBSERVATION_FIELDS = ImmutableList.of(
      "_matched_sample_id",
      "_sample_id",
      "_specimen_id",
      "alignment_algorithm",
      "analysis_id",
      "analyzed_sample_id",
      "base_calling_algorithm",
      "biological_validation_status",
      "marking",
      "matched_sample_id",
      "mutant_allele_read_count",
      "observation_id",
      "other_analysis_algorithm",
      "platform",
      "raw_data_accession",
      "raw_data_repository",
      "seq_coverage",
      "sequencing_strategy",
      "total_read_count",
      "variation_calling_algorithm",
      "verification_status");

  private static final ImmutableList<String> VALID_OCCURRENCE_FIELDS = ImmutableList.of(
      "_donor_id",
      "_mutation_id",
      "_project_id",
      "_type",
      "assembly_version",
      "chromosome",
      "chromosome_end",
      "chromosome_start",
      "chromosome_strand",
      "consequence",
      "mutated_from_allele",
      "mutated_to_allele",
      "mutation",
      "mutation_type",
      "observation",
      "reference_genome_allele");

  private static final String PROJECT_NAME = "BRCA-UK";
  private static final String EMPTY_PROJECT_NAME = "EMPTY";
  private static final List<String> VALID_SAMPLES = ImmutableList.of("ASID1", "ASID2");

  JoinJob job;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    this.job = new JoinJob();
  }

  @Test
  public void executeTest() {
    given(new File(TEST_FIXTURES_DIR));
    val jobContext = createJobContext(job.getType(), asList(PROJECT_NAME, EMPTY_PROJECT_NAME));
    job.execute(jobContext);

    validateClinicalResults(produces(PROJECT_NAME, FileType.CLINICAL));
    validateEmptyProjectClinicalResults(produces(EMPTY_PROJECT_NAME, FileType.CLINICAL));

    validateOccurrences(produces(PROJECT_NAME, FileType.OBSERVATION));

    validatePrimaryMeta(produces(PROJECT_NAME, FileType.PEXP), 20);
    validatePrimaryMeta(produces(PROJECT_NAME, FileType.JCN), 45);
    validatePrimaryMeta(produces(PROJECT_NAME, FileType.MIRNA_SEQ), 30);
    validatePrimaryMeta(produces(PROJECT_NAME, FileType.METH_SEQ), 27);
    validatePrimaryMeta(produces(PROJECT_NAME, FileType.EXP_SEQ), 23);
    validatePrimaryMeta(produces(PROJECT_NAME, FileType.EXP_ARRAY), 18);
    validatePrimaryMeta(produces(PROJECT_NAME, FileType.METH_ARRAY), 21);

    validateSecondaryType(produces(PROJECT_NAME, FileType.CNSM), 40);
    validateSecondaryType(produces(PROJECT_NAME, FileType.SGV), 34);
    validateStsm(produces(PROJECT_NAME, FileType.STSM), 46);

    validateSsm(produces(PROJECT_NAME, FileType.SSM));
  }

  private static void validateSsm(List<ObjectNode> results) {
    assertThat(results).hasSize(2);
    for (val occurrence : results) {
      assertThat(keys(occurrence)).hasSize(16);
      assertThat(occurrence.get("observation").size()).isGreaterThan(0);
      assertThat(occurrence.get("consequence").size()).isGreaterThan(0);
    }

  }

  private static void validateStsm(List<ObjectNode> produces, int fieldsCount) {
    validatePrimaryMeta(produces, fieldsCount);
    val occurrence = produces.get(0);
    assertThat(getElements(occurrence.get("consequence"))).isEmpty();
  }

  private static void validateSecondaryType(List<ObjectNode> results, int fieldsCount) {
    log.debug("{}", results);
    validatePrimaryMeta(results, fieldsCount);
    val occurrence = results.get(0);

    val consequences = occurrence.get("consequence");
    assertThat(consequences.size()).isGreaterThan(0);
    assertThat(keys(consequences.get(0)).size()).isGreaterThan(2);
  }

  private static void validatePrimaryMeta(List<ObjectNode> results, int expectedFieldsCount) {
    log.debug("{}", results);
    assertThat(results).hasSize(1);
    val joined = results.get(0);
    assertThat(keys(joined)).hasSize(expectedFieldsCount);
  }

  private static void validateOccurrences(List<ObjectNode> results) {
    assertThat(results).hasSize(2);

    for (val occurrence : results) {
      validateOccurrenceStructure(occurrence);

      if (textValue(occurrence, "_donor_id").equals("DO001")) {
        validateDO1Occurrence(occurrence);
      }
      else {
        validateDO2Occurrence(occurrence);
      }
    }
  }

  private static void validateDO2Occurrence(ObjectNode occurrence) {
    assertThat(occurrence.get("observation").size()).isEqualTo(2);
  }

  private static void validateDO1Occurrence(ObjectNode occurrence) {
    assertThat(occurrence.get("observation").size()).isEqualTo(1);
  }

  private static void validateOccurrenceStructure(ObjectNode occurrence) {
    val fields = ImmutableList.copyOf(occurrence.fieldNames());
    assertThat(VALID_OCCURRENCE_FIELDS).containsOnlyElementsOf(fields);

    validateArrayNode(occurrence.get("consequence"));
    validateArrayNode(occurrence.get("observation"));

    validateConsequenceStructure(occurrence.get("consequence").get(0));
    validateObservationStructure(occurrence.get("observation").get(0));
  }

  private static void validateObservationStructure(JsonNode observation) {
    assertThat(COMMON_OBSERVATION_FIELDS).containsOnlyElementsOf(getFields(observation));
  }

  private static void validateConsequenceStructure(JsonNode consequence) {
    assertThat(VALID_CONSEQUENCE_FIELDS).containsOnlyElementsOf(getFields(consequence));
  }

  private static List<String> getFields(JsonNode node) {
    return ImmutableList.copyOf(node.fieldNames());
  }

  private static void validateArrayNode(JsonNode arrayNode) {
    assertThat(arrayNode.isArray()).isTrue();
    assertThat(arrayNode.size()).isGreaterThan(0);
  }

  private static void validateClinicalResults(List<ObjectNode> results) {
    log.debug("Clinical - {}", results);
    assertThat(results).hasSize(2);
    val donor = results.get(0);
    validateTherapyFamilyExposure(donor.get("therapy"));
    validateTherapyFamilyExposure(donor.get("family"));
    validateTherapyFamilyExposure(donor.get("exposure"));
    validateSpecimen(donor.get("specimen"));
  }

  private static void validateSpecimen(JsonNode specimenArray) {
    assertThat(specimenArray.isArray()).isTrue();
    val specimens = getElements(specimenArray);
    assertThat(specimens).hasSize(1);

    val specimen = specimens.get(0);
    assertThat(keys(specimen)).hasSize(30);
    val samples = getElements(specimen.get("sample"));
    assertThat(samples).hasSize(2);

    samples.stream().forEach(s -> validateSamples(s));
  }

  private static void validateTherapyFamilyExposure(JsonNode node) {
    assertThat(node.isArray()).isTrue();
    val elements = getElements(node);
    assertThat(elements).hasSize(1);
  }

  private static void validateSamples(JsonNode sample) {
    val sampleId = ObjectNodes.textValue(sample, SUBMISSION_ANALYZED_SAMPLE_ID);
    assertThat(VALID_SAMPLES).contains(sampleId);
    // Sample has 9 fields
    assertThat(keys(sample)).hasSize(9);

    if (sampleId.equals("ASID1")) {
      verifyRawSequenceData(sample, 11);
    } else {
      verifyRawSequenceData(sample, 0);
    }

  }

  private static void verifyRawSequenceData(JsonNode sample, int expectedExementsCount) {
    val elements = getElements(sample.get(AVAILABLE_RAW_SEQUENCE_DATA));
    assertThat(elements).hasSize(expectedExementsCount);

    if (expectedExementsCount > 0) {
      elements.stream()
          .forEach(node -> {
            // Each RawSequenceData object has 3 fields
              assertThat(keys(node)).hasSize(3);
              assertThat(node.path(SUBMISSION_ANALYZED_SAMPLE_ID).isMissingNode()).isTrue();
            });
    }
  }

  private static void validateEmptyProjectClinicalResults(List<ObjectNode> results) {
    assertThat(results).hasSize(1);
    val donor = results.get(0);
    assertThat(donor.findPath("therapy").isMissingNode()).isTrue();
    assertThat(donor.findPath("family").isMissingNode()).isTrue();
    assertThat(donor.findPath("exposure").isMissingNode()).isTrue();
    assertThat(donor.findPath("specimen").isMissingNode()).isTrue();
  }

  private static List<JsonNode> getElements(JsonNode node) {
    return copyOf(node.elements());
  }

  private static List<String> keys(JsonNode node) {
    if (node.isObject()) {
      val objectNode = (ObjectNode) node;
      return copyOf(objectNode.fieldNames());
    }

    return emptyList();
  }

}
