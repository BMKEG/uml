"""
Copyright 2011 University of Southern California

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
from django.db import models
from django.conf import settings
from birn.django.db.models.fields import DirPathField
from birn.django.db.models import BaseBirnModel as BirnDjangoModel
from birn.django.authentication.permissions import create_groups
from django.contrib.auth.models import Permission
from django.db.models import signals
from django.contrib.auth.models import User
from django.core import management
from django.db.models.manager import Manager

import os, shutil, sys

HISTOLOGICAL_DIAGNOSIS_ENUM = (
	('Shigellosis colon', 'Shigellosis colon'), # added for clinical diagnosis sample data
	('Protozoal cyst', 'Protozoal cyst'), # added for clinical diagnosis sample data
)

CELLULAR_REFERENCE_ENUM = (
	('astrocyte', 'astrocyte'),
	('neuron', 'neuron'),
	('glia', 'glia'),
	('Schwann cell', 'Schwann cell'),
	('lymphocyte', 'lymphocyte'),
	('erythrocyte', 'erythrocyte'),
	('macrophage', 'macrophage'),
	('neutrophil', 'neurtophil'),
	('eosinophil', 'eosinophil'),
)

ANATOMICAL_CATEGORY_ENUM = (
)

NO_THUMBNAIL_URL = settings.MEDIA_URL + "/css/no_thumb_available.png"

APPLICATION_GROUPS = (
	'curators',
)

def signal_application_groups(app, created_models, verbosity, **kwargs):
	create_groups(APPLICATION_GROUPS)
signals.post_syncdb.connect(signal_application_groups, sender=sys.modules[__name__])

def delete_index(sender, instance, using, **kwargs):
	sender.indexer.delete(instance)


"""
 Base Model class definition for BIRN models
"""
class BaseBirnModel(BirnDjangoModel):
	def get_delete_url(self):
		return settings.SITE_PREFIX + "/delete/confirmation/%s/%s/?delete=%s" % (self._meta.app_label, self.__class__.__name__, unicode(self.pk))

	def get_confirmed_delete_url(self):
		return "%s/delete/confirm/%s/%s/" % (settings.SITE_PREFIX, self._meta.app_label, self.__class__.__name__)

	class Meta:
		abstract = True

class Ontology_Code(BaseBirnModel):
	OntologyCodeID = models.CharField(max_length=20, primary_key=True, verbose_name="Ontology Code ID")
	OntologySource = models.CharField(max_length=200, blank=True, default='', verbose_name="Ontology Source")
	OntologyName = models.CharField(max_length=200, blank=True, default='', verbose_name="Ontology Name")
	TermID = models.CharField(max_length=200, blank=True, default='', verbose_name="Term ID")
	TermName = models.CharField(max_length=200, blank=True, default='', verbose_name="Term Name")
	TermDescription = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Term Description")

	def __unicode__(self):
		return unicode(self.OntologyCodeID)
	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/ontologycode/%s/" % self.OntologyCodeID

	class Meta:
		verbose_name = "Ontology Code"
		verbose_name_plural = "Ontology Codes"
		ordering = ('OntologyCodeID',)

class Genus(BaseBirnModel):
	GenusName = models.CharField(max_length=200, unique=True, verbose_name="Genus Name")
	GenusOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Genus Ontology Code", on_delete=models.SET_NULL)

	def __unicode__(self):
		if self.GenusOntologyCode is not None:
			return unicode(self.GenusName) + u" (" + unicode(self.GenusOntologyCode.pk) + u")"
		else:
			return unicode(self.GenusName)
	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/genus/%i/" % self.id

	def get_images(self):
		image_list = []
		for species in self.species_set.all():
			image_list.extend(species.get_images())
		return image_list

	class Meta:
		verbose_name = "Genus"
		verbose_name_plural = "Genera"
		ordering = ('GenusName',)

class Species(BaseBirnModel):
	Genus = models.ForeignKey(Genus, verbose_name="Genus")
	SpeciesName = models.CharField(max_length=200, null=False, unique=True, verbose_name="Species Name")
	SpeciesSNOMEDCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Species SNOMED Code", on_delete=models.SET_NULL)

	def __unicode__(self):
		if self.SpeciesSNOMEDCode is not None:
			return unicode(self.SpeciesName) + u" (" + unicode(self.SpeciesSNOMEDCode.pk) + u")"
		else:
			return unicode(self.SpeciesName)
	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/species/%i/" % self.id

	def get_images(self):
		image_list = []
		for subject in self.subject_set.all():
			image_list.extend(subject.get_images())
		return image_list

	class Meta:
		verbose_name = "Species"
		verbose_name_plural = "Species"
		ordering = ('Genus','SpeciesName')

	def delete(self):
		self.related_set.clear()

class SubSpecies(BaseBirnModel):
	Species = models.ForeignKey(Species, verbose_name="Species")
	SubSpeciesName = models.CharField(max_length=200, unique=True, verbose_name="Subspecies Name")
	SubSpeciesSNOMEDCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Subspecies SNOMED Code", on_delete=models.SET_NULL)

	def __unicode__(self):
		if self.SubSpeciesSNOMEDCode is not None:
			return unicode(self.SubSpeciesName) + u" (" + unicode(self.SubSpeciesSNOMEDCode.pk) + u")"
		else:
			return unicode(self.SubSpeciesName)
	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/subspecies/%i/" % self.id

	def get_images(self):
		image_list = []
		for subject in self.subject_set.all():
			image_list.extend(subject.get_images())
		return image_list

	class Meta:
		verbose_name = "Subspecies"
		verbose_name_plural = "Subspecies"
		ordering = ('Species', 'SubSpeciesName')

class DateQualifier(BaseBirnModel):
	QualifierName = models.CharField(max_length=200, blank=True, null=True, unique=True, verbose_name="Qualifier Name")

	def __unicode__(self):
		return unicode(self.QualifierName)

	class Meta:
		abstract = True
		ordering = ('QualifierName',)

class DateOfBirthQualifier(DateQualifier):
	class Meta:
		verbose_name = "Date of Birth Qualifier"
		verbose_name_plural = "Date of Birth Qualifiers"

class DateOfDeathQualifier(DateQualifier):
	class Meta:
		verbose_name = "Date of Death Qualifier"
		verbose_name_plural = "Date of Death Qualifiers"

class CommonName(BaseBirnModel):
	Name = models.CharField(max_length=200, unique=True, verbose_name="Name")
	
	def __unicode__(self):
		return unicode(self.Name)
	
	def get_images(self):
		images = []
		for subject in self.subject_set.all():
			images.extend(subject.get_images())
		return images

	class Meta:
		verbose_name = "Common Name"
		verbose_name_plural = "Common Names"
		ordering = ('Name',)

class AccessionIdentifier(BaseBirnModel):
	AccessionID = models.CharField(max_length=200, unique=True, primary_key=True, verbose_name="Accession ID")

	def __unicode__(self):
		return unicode(self.AccessionID)

	class Meta:
		verbose_name="Accession Identifier"
		verbose_name_plural = "Accession Identifiers"
		ordering = ('AccessionID',)

class Sex(BaseBirnModel):
	Label = models.CharField(max_length=50, unique=True, null=False, blank=False, verbose_name="Label")
	
	def __unicode__(self):
		return unicode(self.Label)

	class Meta:
		verbose_name = "Sex"
		verbose_name_plural = "Sexes"
		ordering = ["Label"]

class Subject(BaseBirnModel):
	SubjectID = models.SlugField(max_length=20, unique=True, null=False, blank=False, verbose_name="Subject ID")
	Species = models.ForeignKey(Species, verbose_name="Species")
	SubSpecies = models.ForeignKey(SubSpecies, null=True, blank=True, verbose_name="Subspecies", on_delete=models.SET_NULL)
	AccessionID = models.ForeignKey(AccessionIdentifier, null=True, blank=True, verbose_name="Accession ID", on_delete=models.SET_NULL)
	CommonName = models.ForeignKey(CommonName, null=True, blank=True, verbose_name="Common Name", on_delete=models.SET_NULL)
	Sex = models.ForeignKey(Sex, null=False, blank=False, verbose_name="Sex")
	DateOfBirth = models.DateField(null=True, blank=True, verbose_name="Date of Birth")
	DateOfBirthQualifier = models.ForeignKey(DateOfBirthQualifier, null=True, blank=True, verbose_name="Date of Birth Qualifier", on_delete=models.SET_NULL)
	DateOfDeath = models.DateField(null=True, blank=True, verbose_name="Date of Death")
	DateOfDeathQualifier = models.ForeignKey(DateOfDeathQualifier, null=True, blank=True, verbose_name="Date of Death Qualifier", on_delete=models.SET_NULL)
	Comments = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Comments")

	def __unicode__(self):
		return unicode(self.SubjectID)

	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/subject/%s/" % self.SubjectID

	def get_edit_url(self):
		return settings.SITE_PREFIX + "/edit/subject/%s/" % self.SubjectID

	def get_edit_cases_url(self):
		return settings.SITE_PREFIX + "/edit/subject/%s/case/" % self.SubjectID

	def get_add_case_url(self):
		return settings.SITE_PREFIX + "/add/case/?subject=%s" % self.SubjectID

	def Genus(self):
		return self.Species.Genus

	def get_images(self):
		images = []
		for case in self.case_set.all():
			for image in case.get_images():
				images.append(image)
		return images

	class Meta:
		verbose_name = "Subject"
		verbose_name_plural = "Subjects"
		ordering = ('SubjectID',)
	def pre_delete(self, **kwargs):
		self.indexer.delete(self)

signals.pre_delete.connect(delete_index, sender=Subject)

class Procedure(BaseBirnModel):
	ProcedureName = models.CharField(max_length=200, unique=True, verbose_name="Procedure Name")
	ProcedureDescription = models.TextField(max_length=2000, blank=True, null=True, default='', verbose_name="Procedure Description")
	ProcedureOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Procedure Ontology Code", on_delete=models.SET_NULL)

	def __unicode__(self):
		if self.ProcedureOntologyCode is not None:
			return unicode(self.ProcedureName) + u" (" + unicode(self.ProcedureOntologyCode) + u")"
		else:
			return unicode(self.ProcedureName)
	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/procedure/%i/" % self.id

	class Meta:
		verbose_name = "Procedure"
		verbose_name_plural = "Procedures"
		ordering = ('ProcedureName',)

class AgeCategory(BaseBirnModel):
	AgeCategoryName = models.CharField(max_length=200, unique=True, verbose_name="Age Category")
	AgeCategoryOntology = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Age Category Ontology", on_delete=models.SET_NULL)

	def __unicode__(self):
		if self.AgeCategoryOntology is not None:
			return unicode(self.AgeCategoryName) + u" (" + unicode(self.AgeCategoryOntology.pk) + u")"
		else:
			return unicode(self.AgeCategoryName)
	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/agecategory/%i/" % self.id

	def get_images(self):
		image_list = []
		for case in self.case_set.all():
			image_list.extend(case.get_images())
		return image_list

	class Meta:
		verbose_name = "Age Category"
		verbose_name_plural = "Age Categories"
		ordering = ('AgeCategoryName',)

class Disease(BaseBirnModel):
        DiseaseName = models.CharField(max_length=200, unique=True, verbose_name="Disease Name")
        DiseaseDescription = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Disease Description")
        DiseaseOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Disease Ontology Code", on_delete=models.SET_NULL)

        def __unicode__(self):
                if self.DiseaseOntologyCode is not None:
                        return unicode(self.DiseaseName) + u" (" + unicode(self.DiseaseOntologyCode) + u")"
                else:
                        return unicode(self.DiseaseName)
        def get_absolute_url(self):
                return settings.SITE_PREFIX + "/disease/%i/" % self.id

        def get_images(self):
                images = []
                for clinical in self.clinicaldiagnoseddisease_set.all():
                        images.extend(clinical.get_images())
                for histo in self.histodiagnoseddisease_set.all():
                        images.extend(histo.get_images())
                return images

        class Meta:
                verbose_name = "Disease"
                verbose_name_plural = "Diseases"
                ordering = ('DiseaseName',)

        def pre_delete(self, **kwargs):
                self.indexer.delete(self)
signals.pre_delete.connect(delete_index, sender=Disease)

class Etiology(BaseBirnModel):
        EtiologyName = models.CharField(max_length=200, unique=True, verbose_name="Etiology Name")
        EtiologyOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Etiology Ontology Code", on_delete=models.SET_NULL)

        def __unicode__(self):
                if self.EtiologyOntologyCode is not None:
                        return unicode(self.EtiologyName) + u" (" + unicode(self.EtiologyOntologyCode) + u")"
                else:
                        return unicode(self.EtiologyName)
        def get_absolute_url(self):
                return settings.SITE_PREFIX + "/etiology/%i/" % self.id

	def get_images(self):
		images = []
                for clinical in self.clinicaldiagnoseddisease_set.all():
                        images.extend(clinical.get_images())
                for histo in self.histodiagnoseddisease_set.all():
                        images.extend(histo.get_images())
                return images

        class Meta:
                verbose_name = "Etiology"
                verbose_name_plural = "Etiologies"
                ordering = ('EtiologyName',)

class DiseaseProcess(BaseBirnModel):
        DiseaseProcessName = models.CharField(max_length=200, unique=True, verbose_name="Disease Process Name")
        DiseaseProcessOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Disease Process Ontology Code", on_delete=models.SET_NULL)

        def __unicode__(self):
                if self.DiseaseProcessOntologyCode is not None:
                        return unicode(self.DiseaseProcessName) + u" (" + unicode(self.DiseaseProcessOntologyCode.pk) + u")"
                else:
                        return unicode(self.DiseaseProcessName)
        def get_absolute_url(self):
                return settings.SITE_PREFIX + "/diseaseprocess/%i/" % self.id

        def get_images(self):
            images = []
            for disease in self.histodiagnoseddisease_set.all():
                images.extend(disease.get_images())
            for disease in self.clinicaldiagnoseddisease_set.all():
                images.extend(disease.get_images())
            return list(set(images))

        class Meta:
                verbose_name = "Disease Process"
                verbose_name_plural = "Disease Processes"
                ordering = ('DiseaseProcessName',)

class MorphologyCode(BaseBirnModel):
        MorphologyCodeName = models.CharField(max_length=200, unique=True, verbose_name="Morphology Code Name")
        MorphologyOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Morphology Ontology Code", on_delete=models.SET_NULL)

        def __unicode__(self):
                if self.MorphologyOntologyCode is not None:
                        return unicode(self.MorphologyCodeName) + u" (" + unicode(self.MorphologyOntologyCode) + u")"
                else:
                        return unicode(self.MorphologyCodeName)

        def get_absolute_url(self):
                return settings.SITE_PREFIX + "/morphologycode/%i/" % self.id

	def get_images(self):
		images = []
                for clinical in self.clinicaldiagnoseddisease_set.all():
                        images.extend(clinical.get_images())
                for histo in self.histodiagnoseddisease_set.all():
                        images.extend(histo.get_images())
                return images

        class Meta:
                verbose_name = "Morphology"
                verbose_name_plural = "Morphologies"
                ordering = ('MorphologyCodeName',)

class HistoDiagnosisCode(BaseBirnModel):
        HistoDiagnosisCodeName = models.CharField(max_length=200, unique=True, verbose_name="Histo Diagnosis Code Name")
        HistoDiagnosisOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Histo Diagnosis Ontology Code", on_delete=models.SET_NULL)

        def __unicode__(self):
                if self.HistoDiagnosisOntologyCode is not None:
                        return unicode(self.HistoDiagnosisCodeName) + u" (" + unicode(self.HistoDiagnosisOntologyCode.pk) + u")"
                else:
                        return unicode(self.HistoDiagnosisCodeName)
        def get_absolute_url(self):
                return settings.SITE_PREFIX + "/histodiagnosiscode/%i/" % self.id

        class Meta:
                verbose_name="Histological Diagnosis Code"
                verbose_name_plural="Histological Diagnosis Codes"
                ordering = ('HistoDiagnosisCodeName',)

class DiagnosedDisease(BaseBirnModel):
        Disease = models.ForeignKey(Disease, null=False, blank=False, verbose_name="Disease", on_delete=models.DO_NOTHING)
        Etiology = models.ForeignKey(Etiology, null=True, blank=True, verbose_name="Etiology", on_delete=models.SET_NULL)
        EtiologyDesc = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Etiology Description")
        DiseaseProcess = models.ManyToManyField(DiseaseProcess, null=True, blank=True, verbose_name="Disease Processes")
        DiseaseProcessDesc = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Disease Process Description")
        MorphologyCode = models.ForeignKey(MorphologyCode, null=True, blank=True, verbose_name="Morphology Code", on_delete=models.SET_NULL)
        MorphologyCodeDesc = models.TextField(max_length=2000, null=True, blank=True, verbose_name="Morphology Description")
        HistoDiagnosisCode = models.ForeignKey(HistoDiagnosisCode, null=True, blank=True, verbose_name="Histo Diagnosis Code", on_delete=models.SET_NULL)
        HistoDiagnosisCodeDesc = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Histo Diagnosis Description")
        TopographicalLocation = models.ForeignKey("TopographicalLocation", null=True, blank=True, verbose_name="Topographical Location")
	def __unicode__(self):
		return unicode(self.Disease)

	class Meta:
		abstract = True

class Clinical_Diagnosis(BaseBirnModel):
        CaseHistoryComments = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Case History Comments")

	def __unicode__(self):
		return unicode(self.pk)

        def get_absolute_url(self):
                return settings.SITE_PREFIX + "/clinicaldiagnosis/%i/" % self.pk

        def Subject(self):
                return self.case.Subject

        def get_images(self):
                return self.case.get_images()

        class Meta:
                verbose_name = "Clinical Diagnosis"
                verbose_name_plural = "Clinical Diagnoses"

        def pre_delete(self, **kwargs):
                self.indexer.delete(self)
signals.pre_delete.connect(delete_index, sender=Clinical_Diagnosis)

class ClinicalDiagnosedDisease(DiagnosedDisease):
	ClinicalDiagnosis = models.ForeignKey(Clinical_Diagnosis, blank=True, null=True, verbose_name="Clinical Diagnosis", on_delete=models.SET_NULL)
	Transmission = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Transmission")

	def pre_delete(self, **kwargs):
		self.indexer.delete(**kwargs)

	def get_images(self):
		return self.ClinicalDiagnosis.get_images()

        class Meta:
                verbose_name = "Clinical Diagnosed Disease"
                verbose_name_plural = "Clinical Diagnosed Diseases"
signals.pre_delete.connect(delete_index, sender=ClinicalDiagnosedDisease)

class Case(BaseBirnModel):
	CaseID = models.SlugField(max_length=20, unique=True, null=False, blank=False, verbose_name="Case ID")
	CaseDate = models.DateField(blank=True, null=True, verbose_name="Case Date")
	Procedure = models.ManyToManyField(Procedure, verbose_name="Procedure")
	Subject = models.ForeignKey(Subject, verbose_name="Subject")
	AgeInDays = models.IntegerField(blank=True, null=True, verbose_name="Age (in days)")
	AgeInYears = models.FloatField(blank=True, null=True, verbose_name="Age (in years)")
	AgeCategory = models.ForeignKey(AgeCategory, blank=True, null=True, verbose_name="Age Category", on_delete=models.SET_NULL)
	BodyWeight = models.FloatField(blank=True, null=True, verbose_name="Body Weight (kg)")
	ClinicalDiagnosis = models.OneToOneField(Clinical_Diagnosis, verbose_name="Clinical Diagnosis", blank=True, null=True)
	Comments = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Comments")
	Published = models.BooleanField(null=False, blank=False, default=False, verbose_name="Published?")
	Publisher = models.ForeignKey(User, null=True, blank=True, verbose_name="Publisher", related_name="+")
	PublishedDate = models.DateTimeField(null=True, blank=True, verbose_name="Published Date")

	def __unicode__(self): 
		return unicode(self.CaseID)

	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/case/%s/" % self.CaseID

	def get_edit_url(self):
		return settings.SITE_PREFIX + "/edit/case/%s/" % self.CaseID

	def get_edit_specimens_url(self):
		return settings.SITE_PREFIX + "/edit/case/%s/specimen/" % self.CaseID

	def get_add_specimen_url(self):
		return settings.SITE_PREFIX + "/add/specimen/?case=%s" % self.CaseID

	def get_add_image_url(self):
		return settings.SITE_PREFIX + "/add/image/?case=%s" % self.CaseID

	def Species(self):
		return self.Subject.Species

	def SubSpecies(self):
		return self.Subject.SubSpecies

	def Genus(self):
		return self.Subject.Species.Genus

	def Procedures(self):
		s = ""
		if self.Procedure:
			for procedure in self.Procedure.all():
				s = s + str(procedure) + "\n"
		return s

	def Diseases(self):
		disease_list = ''
		diseases = self.ClinicalDiagnosis.histodiagnoseddisease_set.all()
		for disease in diseases:
			if len(disease_list) > 0:
				disease_list += ', '
			disease_list += disease.Disease.DiseaseName
				
		return disease_list

	def get_images(self):
		images = []
		for image in self.image_set.all():
			images.append(image)
		for specimen in self.specimen_set.all():
			for image in specimen.get_images():
				images.append(image)
		return images

	class Meta:
		verbose_name = "Case"
		verbose_name_plural = "Cases"
		ordering = ('CaseID',)

	def pre_delete(self, **kwargs):
		self.indexer.delete(self)

signals.pre_delete.connect(delete_index, sender=Case)

class Organization(BaseBirnModel):
	OrganizationName = models.CharField(max_length=100, null=False, blank=False, unique=True, verbose_name="Organization Name")

	def __unicode__(self):
		return unicode(self.OrganizationName)
	
	class Meta:
		verbose_name = "Organization"
		verbose_name_plural = "Organizations"
		ordering = ("OrganizationName",)

class Project(BaseBirnModel):
	ProjectName = models.CharField(max_length=100, null=False, blank=False, verbose_name="Project Name")
	Owner = models.ForeignKey(User, null=False, blank=False, verbose_name="Owner")
	Investigators = models.ManyToManyField(User, null=True, blank=True, verbose_name="Investigators", related_name='+')
	Organizations = models.ManyToManyField(Organization, null=True, blank=True, verbose_name="Organizations")
	ProjectPurpose = models.TextField(max_length=1000, null=True, blank=True, verbose_name="Project Purpose")
	Funding = models.CharField(max_length=100, null=True, blank=True, verbose_name="Funding")
	Cases = models.ManyToManyField(Case, null=True, blank=True, verbose_name="Cases")

	def get_edit_url(self):
		return "%s/edit/project/%i/" % (settings.SITE_PREFIX, self.pk)

	def __unicode__(self):
		return unicode(self.ProjectName)

	def get_edit_cases_url(self):
		return "%s/edit/project/%i/case/" % (settings.SITE_PREFIX, self.pk)

	def get_add_cases_url(self):
		return "%s/edit/project/%i/add/case/" % (settings.SITE_PREFIX, self.pk)

	class Meta:
		verbose_name = "Project"
		verbose_name_plural = "Projects"
		ordering = ("ProjectName",)

class JournalArticle(BaseBirnModel):
	ArticleName = models.CharField(max_length=100, null=False, blank=False, verbose_name="Article Name")
	Project = models.ForeignKey(Project, null=False, blank=False, verbose_name="Project")

	def __unicode__(self):
		return unicode(self.ArticleName)

	class Meta:
		verbose_name = "Journal Article"
		verbose_name_plural = "Journal Articles"
		ordering = ("ArticleName",)

class System(BaseBirnModel):
	SystemName = models.CharField(max_length=200, unique=True, verbose_name="System Name")
	SystemOntologyCode = models.ForeignKey(Ontology_Code, blank=True, null=True, verbose_name="System Ontology Code", on_delete=models.SET_NULL)

	def __unicode__(self):
		if self.SystemOntologyCode is not None:
			return unicode(self.SystemName) + u" (" + unicode(self.SystemOntologyCode.pk) + u")"
		else:
			return unicode(self.SystemName)
	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/system/%i/" % self.id

	def get_images(self):
		image_list = []
		for organ in self.organ_set.all():
			image_list.extend(organ.get_images())
		return image_list

	class Meta:
		verbose_name = "System"
		verbose_name_plural = "Systems"
		ordering = ('SystemName',)

class Organ(BaseBirnModel):
	System = models.ManyToManyField(System, null=True, blank=True, verbose_name="System")
	OrganName = models.CharField(max_length=200, unique=True, verbose_name="Organ Name")
	OrganOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Organ Ontology Code", on_delete=models.SET_NULL)

	def __unicode__(self):
		if self.OrganOntologyCode is not None:
			return unicode(self.OrganName) + u" (" + unicode(self.OrganOntologyCode.pk) + u")"
		else:
			return unicode(self.OrganName)

	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/organ/%i/" % self.id

	def get_images(self):
		images = []
		for specimen in self.specimen_set.all():
			images.extend(specimen.get_images())
		return images

	def Systems(self):
		system_list = ''
		for system in self.System.all():
			if len(system_list) > 0:
				system_list += ', '
			system_list += system.SystemName
		return system_list

	class Meta:
		verbose_name = "Organ"
		verbose_name_plural = "Organs"
		ordering = ('OrganName',)

	def pre_delete(self, **kwargs):
		self.indexer.delete(self)
signals.pre_delete.connect(delete_index, sender=Organ)

class Tissue(BaseBirnModel):
	TissueName = models.CharField(max_length=200, unique=True, verbose_name="Tissue Name")
	TissueOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Tissue Ontology Code", on_delete=models.SET_NULL)

	def __unicode__(self):
		if self.TissueOntologyCode is not None:
			return unicode(self.TissueName) + u" (" + unicode(self.TissueOntologyCode.pk) + u")"
		else:
			return unicode(self.TissueName)
	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/tissue/%i/" % self.id

	class Meta:
		verbose_name = "Tissue"
		verbose_name_plural = "Tissues"
		ordering = ('TissueName',)

class TopographicalLocation(BaseBirnModel):
	TopographicalLocationName = models.CharField(max_length=200, unique=True, verbose_name="Topographical Location Name")
	TopographicalLocationOntologyCode = models.ForeignKey(Ontology_Code, null=True, blank=True, verbose_name="Topographical Location Ontology Code", on_delete=models.SET_NULL)

	def __unicode__(self):
		if self.TopographicalLocationOntologyCode is not None:
			return unicode(self.TopographicalLocationName) + u" (" + unicode(self.TopographicalLocationOntologyCode) + u")"
		else:
			return unicode(self.TopographicalLocationName)
	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/topographicallocation/%i/" % self.id

	def get_images(self):
		images = []
                for clinical in self.clinicaldiagnoseddisease_set.all():
                        images.extend(clinical.get_images())
                for histo in self.histodiagnoseddisease_set.all():
                        images.extend(histo.get_images())
		for specimen in self.specimen_set.all():
			images.extend(specimen.get_images())
                return images

	class Meta:
		verbose_name = "Topographical Location"
		verbose_name_plural = "Topographical Locations"
		ordering = ('TopographicalLocationName',)

class Histo_Diagnosis(BaseBirnModel):
        Comments = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Histological Diagnosis Comments")

	def __unicode__(self):
		return unicode(self.pk)

        def get_absolute_url(self):
                return settings.SITE_PREFIX + "/histodiagnosis/%i/" % self.pk

	def get_images(self):
		return self.specimen.get_images()

        class Meta:
                verbose_name="Histological Diagnosis"
                verbose_name_plural = "Histological Diagnoses"

        def pre_delete(self, **kwargs):
                self.indexer.delete(self)
signals.pre_delete.connect(delete_index, sender=Histo_Diagnosis)

class HistoDiagnosedDisease(DiagnosedDisease):
        HistoDiagnosis = models.ForeignKey(Histo_Diagnosis, blank=True, null=True, verbose_name="Histological Diagnosis", on_delete=models.SET_NULL)

	def pre_delete(self, **kwargs):
		self.indexer.delete(**kwargs)

	def get_images(self):
		return self.HistoDiagnosis.get_images()

        class Meta:
                verbose_name = "Histo Diagnosed Disease"
                verbose_name_plural = "Histo Diagnosed Diseases"
signals.pre_delete.connect(delete_index, sender=HistoDiagnosedDisease)

class Specimen(BaseBirnModel):
	SpecimenID = models.SlugField(max_length=20, unique=True, null=False, blank=False, verbose_name="Specimen ID")
	Case = models.ForeignKey(Case, null=True, blank=True, verbose_name="Case", on_delete=models.SET_NULL)
	Organ = models.ManyToManyField(Organ, verbose_name="Organs")
	Tissue = models.ForeignKey(Tissue, null=True, blank=True, verbose_name="Tissue", on_delete=models.SET_NULL)
	OrganVolume = models.FloatField(null=True, blank=True, verbose_name="Organ Volume (cc)")
	ParanchymalVolume = models.FloatField(null=True, blank=True, verbose_name="Paranchymal Volume (cc)")
	NonparanchymalVolume = models.FloatField(null=True, blank=True, verbose_name="Nonparanchymal Volume (cc)")
	HistologicalDiagnosis = models.OneToOneField(Histo_Diagnosis, verbose_name="Histological Diagnosis", null=True, blank=True)
	CellularReference = models.CharField(max_length=200, blank=True, default='', choices=CELLULAR_REFERENCE_ENUM, verbose_name="Cellular Reference")
	AnatomicalCategory = models.CharField(max_length=200, blank=True, default='', choices=ANATOMICAL_CATEGORY_ENUM, verbose_name="Anatomical Category")
	TopographicalLocation = models.ManyToManyField(TopographicalLocation, null=True, blank=True, verbose_name="Topographical Location")
	SampleInStorage = models.CharField(max_length=200, blank=True, default='', verbose_name="Sample In Storage")
	SampleLocation = models.CharField(max_length=200, blank=True, default='', verbose_name="Sample Location")
	Comments = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Comments")

	def __unicode__(self):
		return unicode(self.SpecimenID)

	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/specimen/%s/" % self.SpecimenID

	def get_edit_url(self):
		return settings.SITE_PREFIX + "/edit/specimen/%s/" % self.SpecimenID

	def get_edit_images_url(self):
		return settings.SITE_PREFIX + "/edit/specimen/%s/image/" % self.SpecimenID

	def get_add_image_url(self):
		return settings.SITE_PREFIX + "/add/image/?specimen=%s" % self.SpecimenID

	def get_edit_samplingprofile_url(self):
		return "%s/edit/specimen/%s/samplingprofile/" % (settings.SITE_PREFIX, self.SpecimenID)

	def get_edit_specimenpreparationprofile_url(self):
		return "%s/edit/specimen/%s/specimenpreparationprofile/" % (settings.SITE_PREFIX, self.SpecimenID)

	def Subject(self):
		return self.Case.Subject

	def Systems(self):
		system_list = ''
		for organ in self.Organ.all():
			if len(system_list) > 0:
				system_list += ', '
			system_list += organ.Systems()

		return system_list

	def Diseases(self):
		disease_list = ''
		diseases = self.HistologicalDiagnosis.histodiagnoseddisease_set.all()
		for disease in diseases:
			if len(disease_list) > 0:
				disease_list += ', '
			disease_list += disease.Disease.DiseaseName
				
		return disease_list

	def Organs(self):
		organ_list = ''
		for organ in self.Organ.all():
			if len(organ_list) > 0:
				organ_list += ', '
			organ_list += organ.OrganName
		return organ_list

	def get_images(self):
		images = []
		for image in self.image_set.all():
			images.append(image)
		return images
						
	class Meta:
		verbose_name = "Specimen"
		verbose_name_plural = "Specimens"
		ordering = ('SpecimenID',)

	def pre_delete(self, **kwargs):
		self.indexer.delete(self)
signals.pre_delete.connect(delete_index, sender=Specimen)

class Attachment(BaseBirnModel):
	File = models.FileField(upload_to="files", blank=False, null=False, verbose_name="File")

	def get_absolute_url(self):
		return self.File.url
	def __unicode__(self):
		return unicode(self.get_filename())

	def get_filename(self):
		filename = ""
		if self.File:
			filename = self.File.name[self.File.name.rfind("/") + 1:]
		return filename

	class Meta:
		abstract = True

class FileAttachment(Attachment):
	Case = models.ForeignKey(Case, blank=True, null=True, verbose_name="Case", on_delete=models.SET_NULL)
	Specimen = models.ForeignKey(Specimen, blank=True, null=True, verbose_name="Specimen", on_delete=models.SET_NULL)

	class Meta:
		verbose_name = "File Attachment"
		verbose_name_plural = "File Attachments"

class JournalArticleArtifact(BaseBirnModel):
	JournalArticle = models.ForeignKey(JournalArticle, null=False, blank=False, verbose_name="Journal Article")
	Link = models.URLField(verify_exists=False, max_length=255, null=True, blank=True, verbose_name="Link")
	Attachment = models.FileField(upload_to="files", blank=True, null=True, verbose_name="Attachment")

	def __unicode__(self):
		if self.Link and len(self.Link) > 0:
			return unicode(self.Link)
		else:
			return unicode(self.get_filename())

	def get_filename(self):
		filename = ""
		if self.Attachment:
			filename = self.Attachment.name[self.Attachment.name.rfind("/") + 1:]
		return filename

	def get_download_url(self):
		url = "#"
		if self.Attachment:
			url = self.Attachment.url
		return url

	class Meta:
		verbose_name = "Journal Article Artifact"
		verbose_name_plural = "Journal Article Artifacts"

class ImageType(BaseBirnModel):
	Label = models.CharField(max_length=200, unique=True, null=False, blank=False, verbose_name="Label")
	
	def __unicode__(self):
		return unicode(self.Label)

	class Meta:
		verbose_name = "Image Type"
		verbose_name_plural = "Image Types"
		ordering = ["Label"]

class Image(BaseBirnModel):
	Filename = models.CharField(max_length=255, null=False, blank=False, verbose_name="Filename")
	FileURL = models.CharField(max_length=1000, null=True, blank=True, verbose_name="File URL")
	ThumbnailURL = models.CharField(max_length=1000, null=True, blank=True, verbose_name="Thumbnail URL")
	ViewURL = models.CharField(max_length=1000, null=True, blank=True, verbose_name="View URL") 
	DicomURL = models.CharField(max_length=1000, null=True, blank=True, verbose_name="Dicom URL")
	Specimen = models.ForeignKey(Specimen, null=True, blank=True, verbose_name="Specimen", on_delete=models.SET_NULL)
	Case = models.ForeignKey(Case, null=True, blank=True, verbose_name="Case", on_delete=models.SET_NULL)
	ImageType = models.ForeignKey(ImageType, null=True, blank=True, verbose_name="Image Type")
	ImageCaption = models.TextField(max_length=2000, blank=True, null=True, verbose_name="Image Caption")
	AccessionID = models.ForeignKey(AccessionIdentifier, blank=True, null=True, verbose_name="Accession ID")
	StorageLocation = models.CharField(max_length=200, blank=True, verbose_name="Storage Location")
	DateCreated = models.DateField(blank=True,null=True, verbose_name="Date Created")
	OriginalWidth = models.PositiveIntegerField(null=True, blank=True, verbose_name="Original Width (in pixels)")
	OriginalHeight = models.PositiveIntegerField(null=True, blank=True, verbose_name="Original Height (in pixels)")
	Owner = models.ForeignKey(User, null=True, blank=True, verbose_name="Owner", on_delete=models.SET_NULL)
	DeviceProfile = models.ForeignKey("DeviceProfile", null=True, blank=True, on_delete=models.SET_NULL, verbose_name="Device Profile")
	ImagingTechniques = models.ManyToManyField("ImagingTechnique", null=True, blank=True, verbose_name="Imaging Techniques")

	def __unicode__(self):
		return unicode(self.Filename)

	def get_absolute_url(self):
		return settings.SITE_PREFIX + "/image_details/%i/" % self.id

	def get_edit_url(self):
		return settings.SITE_PREFIX + "/edit/image/%i/" % self.id

	def get_viewer_url(self):
		if self.ViewURL and len(self.ViewURL) > 0:
			return settings.SITE_PREFIX + "/viewer/%i/" % self.id
		elif self.FileURL and len(self.FileURL) > 0:
			return self.FileURL
		return "#"

	def get_open_viewer_url(self):
		if self.ViewURL and len(self.ViewURL) > 0:
			return "javascript:openViewerWindow('%s');" % self.get_viewer_url()
		elif self.FileURL and len(self.FileURL) > 0:
			return self.FileURL
		else:
			return "#"

	def view(self):
		if self.ViewURL is not None and len(self.ViewURL) > 0:
			return self.ViewURL
		return self.FileURL

	def get_thumb_absolute_url(self):
		if self.ThumbnailURL and len(self.ThumbnailURL) > 0:
			return self.ThumbnailURL
		else:
			return NO_THUMBNAIL_URL

	def get_thumb_html(self):
		return "<img class=\"thumbnail\" src=\"" + str(self.get_thumb_absolute_url()) + "\"/>"
	get_thumb_html.short_description = "Image Thumbnail"
	get_thumb_html.allow_tags = True

	def get_download_absolute_url(self):
		return self.FileURL

	def is_recently_uploaded(self):
		return self.Case is None and self.Specimen is None

	def get_image_extension(self):
		from os.path import splitext
		ext = ""
		pieces = splitext(self.Filename)
		ext = pieces[1]
		return ext

	"""
	URL for annotations of this image
	"""
	def get_annotations_absolute_url(self):
		return settings.SITE_PREFIX + "/image/%i/annotations/" % self.id

	def get_images(self):
		return (self,)

	# these methods were added so that Djapian could search within multiple levels of objects (it is limited to 2 but can use methods)
	def Subject(self):
		if self.Specimen and self.Specimen.Case:
			return self.Specimen.Case.Subject

		elif self.Case:
			return self.Case.Subject

	def Species(self):
		subject = self.Subject()
		if subject:
			return subject.Species

	def Genus(self):
		species = self.Species()
		if species:
			return species.Genus

	def SubSpecies(self):
		subject = self.Subject()
		if subject:
			return subject.SubSpecies

	def get_case(self):
		if self.Case:
			return Case
		elif self.Specimen and self.Specimen.Case:
			return self.Specimen.Case

	class Meta:
		verbose_name = "Image"
		verbose_name_plural = "Images"

	def pre_delete(self, **kwargs):
		self.indexer.delete(self)
signals.pre_delete.connect(delete_index, sender=Image)

class ImageMetadata(BaseBirnModel):
	Image = models.ForeignKey(Image, verbose_name="Image")
	MetadataName = models.CharField(max_length=200, verbose_name="Metadata Name")
	MetadataValue = models.CharField(max_length=200, blank=True, default='', verbose_name="Metadata Value")

	def __unicode__(self):
		return unicode(self.MetadataName)
	def get_images(self):
		return [ Image ]

	class Meta:
		verbose_name = "Image Metadata"
		verbose_name_plural = "Image Metadata"
		ordering = ["MetadataName"]

	def pre_delete(self, **kwargs):
		self.indexer.delete(self)
signals.pre_delete.connect(delete_index, sender=ImageMetadata)

class ImageAnnotation(BaseBirnModel):
	Image = models.ForeignKey(Image, verbose_name="Image")
	User = models.ForeignKey(User, null=True, blank=True, verbose_name="User")
	Annotation = models.TextField(null=False, blank=False, verbose_name="Annotation")

	def __unicode__(self):
		return unicode(self.Annotation)

from pathology.birnpath.models import BaseBirnModel

class ImagingTechnique(BaseBirnModel):
	Label = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Label")
	
	def __unicode__(self):
		return unicode(self.Label)

	class Meta:
		verbose_name = "Imaging Technique"
		verbose_name_plural = "Imaging Techniques"
		ordering = ['Label']

class DeviceType(BaseBirnModel):
	DeviceTypeName = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Device Type Name")
	
	def __unicode__(self):
		return unicode(self.DeviceTypeName)

	class Meta:
		verbose_name = "Device Type"
		verbose_name_plural = "Device Types"
		ordering = [ 'DeviceTypeName' ]

class Manufacturer(BaseBirnModel):
	DeviceType = models.ForeignKey(DeviceType, null=False, blank=False, verbose_name="Device Type")
	ManufacturerName = models.CharField(max_length=50, null=False, blank=False, verbose_name="Manufacturer Name")
	
	def __unicode__(self):
		return unicode(self.ManufacturerName)

	class Meta:
		verbose_name = "Manufacturer"
		verbose_name_plural = "Manufacturers"
		ordering = ['ManufacturerName']
		unique_together = ("DeviceType", "ManufacturerName")

class ModelNumber(BaseBirnModel):
	Manufacturer = models.ForeignKey(Manufacturer, null=False, blank=False, verbose_name="Manufacturer")
	Number = models.CharField(max_length=200, null=False, blank=False, verbose_name="Model Number")

	def __unicode__(self):
		return unicode(self.Number)

	class Meta:
		verbose_name = "Model Number"
		verbose_name_plural = "Model Numbers"
		ordering = ['Number']
		unique_together = ("Manufacturer", "Number")

class ObjectiveLensMagnification(BaseBirnModel):
	Magnification = models.FloatField(null=False, blank=False, unique=True, verbose_name="Magnification")
	
	def __unicode__(self):
		return unicode(self.Magnification)

	class Meta:
		verbose_name = "Objective Lens Magnification"
		verbose_name_plural = "Objective Lens Magnifications"
		ordering = ['Magnification']

class ObjectiveLensNumericalAperture(BaseBirnModel):
	ObjectiveLensMagnification = models.ForeignKey(ObjectiveLensMagnification, null=False, blank=False, verbose_name="Objective Lens Magnification")
	Aperture = models.FloatField(null=False, blank=False, verbose_name="Aperture")

	def __unicode__(self):
		return unicode(self.Aperture)

	class Meta:
		verbose_name = "Objective Lens Numerical Aperture"
		verbose_name_plural = "Objective Lens Numerical Apertures"
		ordering = ['Aperture']
		unique_together = ("ObjectiveLensMagnification", "Aperture")

class DeviceProfile(BaseBirnModel):
	ProfileName = models.CharField(max_length=50, null=False, blank=False, unique=True, verbose_name="Profile Name")
	DeviceType = models.ForeignKey(DeviceType, null=True, blank=True, verbose_name="Device Type")
	Manufacturer = models.ForeignKey(Manufacturer, null=True, blank=True, verbose_name="Manufacturer")
	ModelNumber = models.ForeignKey(ModelNumber, null=True, blank=True, verbose_name="Model Number")
	ObjectiveLensMagnification = models.ForeignKey(ObjectiveLensMagnification, null=True, blank=True, verbose_name="Objective Lens Magnifaction")
	ObjectiveLensNumericalAperture = models.ForeignKey(ObjectiveLensNumericalAperture, null=True, blank=True, verbose_name="Object Lens Numerical Aperture")
	OptovarZoomSetting = models.CharField(max_length=50, null=True, blank=True, default="1", verbose_name="Optovar/Zoom Setting")

	def __unicode__(self):
		return unicode(self.ProfileName)

	def get_edit_url(self):
		return "%s/edit/deviceprofile/%s/" % (settings.SITE_PREFIX, unicode(self.pk))

	class Meta:
		verbose_name = "Device Profile"
		verbose_name_plural = "Device Profiles"
		ordering = ['ProfileName']

SPECIMEN_PREPARATION_PROFILE_SAMPLE_TYPES = (
	("Section", "Section"),
	("Whole Mount", "Whole Mount"),
)

class Fixation(BaseBirnModel):
	FixationName = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Fixation Name")
	
	def __unicode__(self):
		return unicode(self.FixationName)

	class Meta:
		verbose_name = "Fixation"
		verbose_name_plural = "Fixations"
		ordering = ['FixationName']

class Preparation(BaseBirnModel):
	Label = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Label")
	
	def __unicode__(self):
		return unicode(self.Label)

	class Meta:
		verbose_name = "Preparation"
		verbose_name_plural = "Preparations"
		ordering = ['Label']

class EmbeddingMedia(BaseBirnModel):
	Preparation = models.ForeignKey(Preparation, null=False, blank=False, verbose_name="Preparation")
	EmbeddingMediaName = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Embedding Media Name")
	
	def __unicode__(self):
		return unicode(self.EmbeddingMediaName)

	class Meta:
		verbose_name = "Embedding Media"
		verbose_name_plural = "Embedding Media"
		ordering = ['EmbeddingMediaName']

class Stain(BaseBirnModel):
	Label = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Label")
	
	def __unicode__(self):
		return unicode(self.Label)

	class Meta:
		verbose_name = "Stain"
		verbose_name_plural = "Stains"
		ordering = ['Label']

class StainingMethod(BaseBirnModel):
	StainComment = models.TextField(max_length=2000, null=True, blank=True, verbose_name="Stain Comment")

	def __unicode__(self):
		return unicode(self.pk)

	class Meta:
		verbose_name = "Staining Method"
		verbose_name_plural = "Staining Methods"

class HistochemicalStain(BaseBirnModel):
	StainingMethod = models.ForeignKey(StainingMethod, null=False, blank=False, verbose_name="Staining Method")
	Target = models.CharField(max_length=200, null=True, blank=True, verbose_name="Target")
	Stain = models.ForeignKey(Stain, null=True, blank=True, verbose_name="Stain")

	def __unicode__(self):
		return "%s - %s" % (unicode(self.Target), unicode(self.Stain))

	class Meta:
		verbose_name = "Histochemical Stain"
		verbose_name_plural = "Histochemical Stains"

class ImmunohistochemicalTarget(BaseBirnModel):
	Label = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Label")

	def __unicode__(self):
		return unicode(self.Label)

	class Meta:
		verbose_name = "Immunohistochemical Target"
		verbose_name_plural = "Immunohistochemical Targets"
		ordering = ['Label']

class Chromogen(BaseBirnModel):
	Label = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Label")

	def __unicode__(self):
		return unicode(self.Label)

	class Meta:
		verbose_name = "Chromogen"
		verbose_name_plural = "Chromogens"
		ordering = ['Label']

class ImmunohistochemicalStain(BaseBirnModel):
	StainingMethod = models.ForeignKey(StainingMethod, null=False, blank=False, verbose_name="Staining Method")	
	ImmunohistochemicalTarget = models.ForeignKey(ImmunohistochemicalTarget, null=True, blank=True, verbose_name="Immunohistochemical Target")	
	
	class Meta:
		abstract = True

class ImmunohistochemicalChromogenStain(ImmunohistochemicalStain):
	Chromogen = models.ForeignKey(Chromogen, null=True, blank=True, verbose_name="Chromogen")

	def __unicode__(self):
		return "%s - %s" % (unicode(self.ImmunohistochemicalTarget), unicode(self.Chromogen))

	class Meta:
		verbose_name = "Immunohistochemical Chromogen Stain"
		verbose_name_plural = "Immunohistochemical Chromogen Stains"

class FluorescentChannel(BaseBirnModel):
	Number = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Number")
	
	def __unicode__(self):
		return unicode(self.Number)

	class Meta:
		verbose_name = "Fluorescent Channel"
		verbose_name_plural = "Fluorescent Channels"
		ordering = ['Number']

class Fluorochrome(BaseBirnModel):
	Label = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Label")

	def __unicode__(self):
		return unicode(self.Label)

	class Meta:
		verbose_name = "Flurochrome"
		verbose_name_plural = "Fluorochromes"
		ordering = ['Label']

class FluorochromeColor(BaseBirnModel):
	Label = models.CharField(max_length=200, null=False, blank=False, unique=True, verbose_name="Label")
	
	def __unicode__(self):
		return unicode(self.Label)

	class Meta:
		verbose_name = "Fluorochrome Color"
		verbose_name_plural = "Fluorochrome Colors"
		ordering = ['Label']

class ImmunohistochemicalFluorochromeStain(ImmunohistochemicalStain):
	FluorescentChannel = models.ForeignKey(FluorescentChannel, null=True, blank=True, verbose_name="Channel")
	Fluorochrome = models.ForeignKey(Fluorochrome, null=True, blank=True, verbose_name="Fluorochrome")
	FluorochromeColor = models.ForeignKey(FluorochromeColor, null=True, blank=True, verbose_name="Color")

	def __unicode__(self):
		return "%s - %s - %s - %s" % (unicode(self.ImmunohistochemicalTarget), unicode(self.FluorescentChannel), unicode(self.Fluorochrome), unicode(self.FluorochromeColor))

	class Meta:
		verbose_name = "Immunohistochemical Fluorochrome Stain"
		verbose_name_plural = "Immunohistochemical Fluorochrome Stains"

class SpecimenPreparationProfile(BaseBirnModel):
	Specimen = models.OneToOneField(Specimen, null=False, blank=False, verbose_name="Specimen")
	SampleType = models.CharField(max_length=12, null=True, blank=True, choices=SPECIMEN_PREPARATION_PROFILE_SAMPLE_TYPES, verbose_name="Sample Type (section/whole mount)")
	Fixation = models.ForeignKey(Fixation, null=True, blank=True, verbose_name="Fixation")
	EmbeddingMedia = models.ForeignKey(EmbeddingMedia, null=True, blank=True, verbose_name="Embedding Media")
	StainingMethod = models.OneToOneField(StainingMethod, null=False, blank=False, verbose_name="Staining Method")
	SectionThickness = models.FloatField(null=True, blank=True, verbose_name="Section Thickness (um)")

	def __unicode__(self):
		return unicode(self.pk)

	class Meta:
		verbose_name = "Specimen Preparation Profile"
		verbose_name_plural = "Specimen Preparation Profiles"

SAMPLING_PROFILE_SMOOTH_FRACTIONATOR_SAMPLING_CHOICES = (
	('Yes', 'Yes'),
	('No', 'No'),
)
SAMPLING_PROFILE_PUNCH_TEMPLATE_SAMPLING_CHOICES = (
	('Yes', 'Yes'),
	('No', 'No'),
)
SAMPLING_PROFILE_ORGAN_CAVALIERI_VOLUME_CALCULATED = (
	('Yes', 'Yes'),
	('No', 'No'),
)
SAMPLING_PROFILE_CAVALIERI_VOLUME_UNITS = (
	('Cubic mm', 'Cubic mm'),
	('Cubic cm', 'Cubic cm'),
)
SAMPLING_PROFILE_ORGAN_VOLUME_DISPLACEMENT_CALCULATED = (
	('Yes', 'Yes'),
	('No', 'No'),
)
SAMPLING_PROFILE_DISPLACEMENT_VOLUME_UNITS = (
	('Cubic mm', 'Cubic mm'),
	('Cubic cm', 'Cubic cm'),
)
SAMPLING_PROFILE_WHOLE_ORGAN_EMBEDDED_FOR_SECTIONING = (
	('Yes', 'Yes'),
	('No', 'No'),
)
SAMPLING_PROFILE_BLOCKS_WERE_EXHAUSTIVELY_SECTIONED = (
	('Yes', 'Yes'),
	('No', 'No'),
)
SAMPLING_PROFILE_SERIAL_SECTIONS_WERE_TAKEN = (
	('Yes', 'Yes'),
	('No', 'No'),
)

class SamplingProfile(BaseBirnModel):

	Specimen = models.OneToOneField(Specimen, null=False, blank=False, verbose_name="Specimen")
	SmoothFractionatorSampling = models.CharField(max_length=3, null=True, blank=True, choices=SAMPLING_PROFILE_SMOOTH_FRACTIONATOR_SAMPLING_CHOICES, verbose_name="Smooth Fractionator Sampling Fraction")
	SmoothFractionatorSamplingFraction = models.FloatField(null=True, blank=True, verbose_name="Smooth Fractionator Sampling Fraction (%)")
	PunchTemplateSampling = models.CharField(max_length=3, null=True, blank=True, choices=SAMPLING_PROFILE_PUNCH_TEMPLATE_SAMPLING_CHOICES, verbose_name="Punch Template Sampling")
	PunchTemplateSamplingFraction = models.FloatField(null=True, blank=True, verbose_name="Punch Template Sampling Fraction (%)")
	OrganCavalieriVolumeCalculated = models.CharField(max_length=3, null=True, blank=True, choices=SAMPLING_PROFILE_ORGAN_CAVALIERI_VOLUME_CALCULATED, verbose_name="Organ Cavalieri Volume Calculated")
	CavalieriVolume = models.FloatField(null=True, blank=True, verbose_name="Cavalieri Volume")
	CavalieriVolumeUnits = models.CharField(max_length=8, null=True, blank=True, verbose_name="Cavalieri Volume Units")
	OrganVolumeDisplacementCalculated = models.CharField(max_length=3, null=True, blank=True, choices=SAMPLING_PROFILE_ORGAN_VOLUME_DISPLACEMENT_CALCULATED, verbose_name="Organ Volume Displacement Calculated")
	VolumeByDisplacement = models.FloatField(null=True, blank=True, verbose_name="Volume by Displacement")
	DisplacementVolumeUnits = models.CharField(max_length=8, null=True, blank=True, choices=SAMPLING_PROFILE_DISPLACEMENT_VOLUME_UNITS, verbose_name="Displacement Volume Units")
	NumberOfSampleBlocksCreated = models.FloatField(null=True, blank=True, verbose_name="Number of Sample Blocks Created")
	WholeOrganEmbeddedForSectioning = models.CharField(max_length=3, null=True, blank=True, choices=SAMPLING_PROFILE_WHOLE_ORGAN_EMBEDDED_FOR_SECTIONING, verbose_name="Whole Organ Embedded for Sectioning")
	BlocksWereExhaustivelySectioned = models.CharField(max_length=3, null=True, blank=True, choices=SAMPLING_PROFILE_BLOCKS_WERE_EXHAUSTIVELY_SECTIONED, verbose_name="Blocks Were Exhaustively Sectioned")
	SerialSectionsWereTaken = models.CharField(max_length=3, null=True, blank=True, choices=SAMPLING_PROFILE_SERIAL_SECTIONS_WERE_TAKEN, verbose_name="Serial Sections Were Taken")
	DistanceBetweenSerialSections = models.FloatField(null=True, blank=True, verbose_name="Distance Between Serial Sections (um)")
	SectionThickness = models.FloatField(null=True, blank=True, verbose_name="Section Thicknesss (um)")
	OrganStructureSamplingFraction = models.FloatField(null=True, blank=True, verbose_name="Organ/Structure Sampling Fraction (%)")
	BlockSamplingFraction1 = models.FloatField(null=True, blank=True, verbose_name="Block Sampling Fraction 1 (%)")
	BlockSamplingFraction2 = models.FloatField(null=True, blank=True, verbose_name="Block Sampling Fraction 2 (%)")
	BlockSamplingFraction3 = models.FloatField(null=True, blank=True, verbose_name="Block Sampling Fraction 3 (%)")
	ArealSamplingFraction = models.FloatField(null=True, blank=True, verbose_name="Areal Sampling Fraction (%)")
	HeightSamplingFraction = models.FloatField(null=True, blank=True, verbose_name="Height Sampling Fraction (%)")

	def __unicode__(self):
		return unicode(self.pk)

	class Meta:
		verbose_name = "Sampling Profile"
		verbose_name_plural = "Sampling Profiles"

