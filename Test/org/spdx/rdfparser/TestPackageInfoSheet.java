package org.spdx.rdfparser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.spdxspreadsheet.OriginsSheet;
import org.spdx.spdxspreadsheet.PackageInfoSheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;

public class TestPackageInfoSheet {
	
	File spreadsheetFile;

	@Before
	public void setUp() throws Exception {
		spreadsheetFile = File.createTempFile("TEST_PKG_INFO", "xls");
	}

	@After
	public void tearDown() throws Exception {
		spreadsheetFile.delete();
	}


	@Test
	public void testCreate() throws IOException, InvalidFormatException {
		
		Workbook wb = new HSSFWorkbook();
		PackageInfoSheet.create(wb, "Package Info");
		PackageInfoSheet pkgInfo = new PackageInfoSheet(wb, "Package Info", OriginsSheet.CURRENT_VERSION);
		String ver = pkgInfo.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}
	}

	@Test
	public void testAddAndGet() throws SpreadsheetException {
		SPDXLicenseInfo[] testLicenses1 = new SPDXLicenseInfo[3];
		testLicenses1[0] = new SPDXNonStandardLicense("License1", "License1Text");
		SPDXLicenseInfo[] disjunctiveLicenses = new SPDXLicenseInfo[3];
		disjunctiveLicenses[0] = new SPDXNonStandardLicense("disj1", "disj1 Text");
		disjunctiveLicenses[1] = new SPDXNonStandardLicense("disj2", "disj2 Text");
		disjunctiveLicenses[2] = new SPDXNonStandardLicense("disj3", "disj3 Text");
		testLicenses1[1] = new SPDXDisjunctiveLicenseSet(disjunctiveLicenses);
		SPDXLicenseInfo[] conjunctiveLicenses = new SPDXLicenseInfo[] {
				new SPDXNonStandardLicense("conj1", "conj1 Text"),
				new SPDXNonStandardLicense("conj2", "conj2 Text")
		};
		testLicenses1[2] = new SPDXConjunctiveLicenseSet(conjunctiveLicenses);
		SPDXLicenseInfo testLicense1 = new SPDXDisjunctiveLicenseSet(testLicenses1);
		
//		String lic1String = PackageInfoSheet.licensesToString(testLicenses1);
		SPDXLicenseInfo[] testLicenses2 = new SPDXLicenseInfo[2];
		testLicenses2[0] = new SPDXNonStandardLicense("License3", "License 3 text");
		testLicenses2[1] = new SPDXNonStandardLicense("License4", "License 4 text");
		SPDXLicenseInfo testLicense2 = new SPDXConjunctiveLicenseSet(testLicenses2);
		SPDXLicenseInfo[] testLicenseInfos = new SPDXLicenseInfo[] {new SPDXNoneLicense()};
//		String lic2String = PackageInfoSheet.licensesToString(testLicenses2);
		SPDXPackageInfo pkgInfo1 = new SPDXPackageInfo("decname1", "machinename1", 
				"sha1-1", "sourceinfo1", testLicense1,
				testLicense2, testLicenseInfos, "license comments", "dec-copyright1",
				"short desc1", "desc1", "http://url1", "filechecksum1");
		SPDXPackageInfo pkgInfo2 = new SPDXPackageInfo("decname1", "machinename1", 
				"sha1-1", "sourceinfo1", testLicense1,
				testLicense2, testLicenseInfos, "licensecomments2", "dec-copyright1",
				"short desc1", "desc1", "http://url1", "filechecksum1");
		Workbook wb = new HSSFWorkbook();
		PackageInfoSheet.create(wb, "Package Info");
		PackageInfoSheet pkgInfoSheet = new PackageInfoSheet(wb, "Package Info", OriginsSheet.CURRENT_VERSION);
		pkgInfoSheet.add(pkgInfo1);
		pkgInfoSheet.add(pkgInfo2);
		SPDXPackageInfo tstPkgInfo1 = pkgInfoSheet.getPackageInfo(1);
		SPDXPackageInfo tstPkgInfo2 = pkgInfoSheet.getPackageInfo(2);
		comparePkgInfo(pkgInfo1, tstPkgInfo1);
		comparePkgInfo(pkgInfo2, tstPkgInfo2);
		SPDXPackageInfo tstPkgInfo3 = pkgInfoSheet.getPackageInfo(3);
		if (tstPkgInfo3 != null) {
			fail("should be null");
		}
	}

	private void comparePkgInfo(SPDXPackageInfo pkgInfo1,
			SPDXPackageInfo pkgInfo2) {
		assertEquals(pkgInfo1.getDeclaredCopyright(), pkgInfo2.getDeclaredCopyright());
		assertEquals(pkgInfo1.getDeclaredLicenses(), pkgInfo2.getDeclaredLicenses());
		assertEquals(pkgInfo1.getConcludedLicense(), pkgInfo2.getConcludedLicense());
		assertEquals(pkgInfo1.getDeclaredName(), pkgInfo2.getDeclaredName());
		assertEquals(pkgInfo1.getDescription(), pkgInfo2.getDescription());
		assertEquals(pkgInfo1.getFileChecksum(), pkgInfo2.getFileChecksum());
		assertEquals(pkgInfo1.getFileName(), pkgInfo2.getFileName());
		assertEquals(pkgInfo1.getSha1(), pkgInfo2.getSha1());
		assertEquals(pkgInfo1.getShortDescription(), pkgInfo2.getShortDescription());
		assertEquals(pkgInfo1.getSourceInfo(), pkgInfo2.getSourceInfo());
		assertEquals(pkgInfo1.getUrl(), pkgInfo2.getUrl());
		if (!compareLicenses(pkgInfo1.getLicensesFromFiles(), pkgInfo2.getLicensesFromFiles())) {
			fail("license infos not equal");
		}
	}

	/**
	 * @param license1
	 * @param licenses2
	 */
	private boolean compareLicenses(SPDXLicenseInfo[] licenses1,
			SPDXLicenseInfo[] licenses2) {
		if (licenses1.length != licenses2.length) {
			return false;
		}
		for (int i = 0; i < licenses1.length; i++) {
			boolean found = false;
			for (int j = 0; j < licenses2.length; j++) {
				if (licenses1[i].equals(licenses2[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	static public void compareLicenseDeclarations(
			SPDXLicenseInfo[] testLicenses,
			SPDXLicenseInfo[] result) {
		assertEquals(testLicenses.length, result.length);
		for (int i = 0;i < testLicenses.length; i++) {
			boolean found = false;
			for (int j = 0; j < result.length; j++) {
				if (testLicenses[i].equals(result[j])) {
					found = true;
				}
			}				
			if (!found) {
				fail("License match "+testLicenses[i].toString()+ " was not found.");
			}
		}
	}
}
