const fs = require('fs');
const path = require('path');
const { XMLParser } = require('fast-xml-parser');

function findFiles(pattern, rootDir = '.') {
    const results = [];
    
    function searchDir(dir) {
        try {
            const entries = fs.readdirSync(dir, { withFileTypes: true });
            
            for (const entry of entries) {
                const fullPath = path.join(dir, entry.name);
                
                if (entry.isDirectory()) {
                    if (!entry.name.startsWith('.') && entry.name !== 'node_modules') {
                        searchDir(fullPath);
                    }
                } else if (entry.isFile()) {
                    if (pattern.test(fullPath)) {
                        results.push(fullPath);
                    }
                }
            }
        } catch (err) {
        }
    }
    
    searchDir(rootDir);
    return results;
}

function parseTestResults() {
    const testFiles = findFiles(/build\/test-results\/test\/.*\.xml$/);
    
    let totalTests = 0;
    let totalFailures = 0;
    let totalErrors = 0;
    let totalSkipped = 0;
    let totalPassed = 0;
    
    const parser = new XMLParser({
        ignoreAttributes: false,
        attributeNamePrefix: '@_'
    });
    
    for (const testFile of testFiles) {
        try {
            const content = fs.readFileSync(testFile, 'utf-8');
            const result = parser.parse(content);
            const testsuite = result.testsuite;
            
            if (testsuite) {
                const tests = parseInt(testsuite['@_tests'] || '0', 10);
                const failures = parseInt(testsuite['@_failures'] || '0', 10);
                const errors = parseInt(testsuite['@_errors'] || '0', 10);
                const skipped = parseInt(testsuite['@_skipped'] || '0', 10);
                
                totalTests += tests;
                totalFailures += failures;
                totalErrors += errors;
                totalSkipped += skipped;
                totalPassed += (tests - failures - errors - skipped);
            }
        } catch (err) {
            continue;
        }
    }
    
    return {
        total: totalTests,
        passed: totalPassed,
        failed: totalFailures,
        errors: totalErrors,
        skipped: totalSkipped
    };
}

function parseCoverage() {
    const coverageFile = 'build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml';
    
    if (!fs.existsSync(coverageFile)) {
        return null;
    }
    
    try {
        const content = fs.readFileSync(coverageFile, 'utf-8');
        const parser = new XMLParser({
            ignoreAttributes: false,
            attributeNamePrefix: '@_',
            parseAttributeValue: true
        });
        
        const result = parser.parse(content);
        const report = result.report;
        
        if (!report) {
            return null;
        }
        
        function findCounters(node, counters = {}) {
            if (node.counter) {
                const counterArray = Array.isArray(node.counter) ? node.counter : [node.counter];
                
                for (const counter of counterArray) {
                    const type = counter['@_type'];
                    const missed = parseInt(counter['@_missed'] || '0', 10);
                    const covered = parseInt(counter['@_covered'] || '0', 10);
                    const total = missed + covered;
                    
                    if (total > 0) {
                        if (!counters[type]) {
                            counters[type] = { missed: 0, covered: 0, total: 0 };
                        }
                        counters[type].missed += missed;
                        counters[type].covered += covered;
                        counters[type].total += total;
                    }
                }
            }
            
            for (const key in node) {
                if (key !== 'counter' && typeof node[key] === 'object' && node[key] !== null) {
                    findCounters(node[key], counters);
                }
            }
            
            return counters;
        }
        
        const counters = findCounters(report);
        
        const coverageData = {};
        for (const [type, data] of Object.entries(counters)) {
            if (data.total > 0) {
                coverageData[type] = {
                    missed: data.missed,
                    covered: data.covered,
                    total: data.total,
                    percentage: (data.covered / data.total) * 100
                };
            }
        }
        
        return coverageData;
    } catch (err) {
        return null;
    }
}

function generateMarkdown() {
    const testResults = parseTestResults();
    const coverage = parseCoverage();
    
    const markdown = [];
    markdown.push('## 테스트 결과\n');
    
    if (testResults.total > 0) {
        const successRate = (testResults.passed / testResults.total) * 100;
        
        markdown.push(`총 테스트: ${testResults.total}`);
        markdown.push(`  - 성공: ${testResults.passed}`);
        markdown.push(`  - 실패: ${testResults.failed}`);
        markdown.push(`  - 오류: ${testResults.errors}`);
        markdown.push(`  - 건너뜀: ${testResults.skipped}`);
        markdown.push(`  - 성공률: ${successRate.toFixed(2)}%\n`);
    } else {
        markdown.push('테스트 결과를 찾을 수 없습니다.\n');
    }
    
    markdown.push('## 코드 커버리지\n');
    
    if (coverage && Object.keys(coverage).length > 0) {
        const instruction = coverage.INSTRUCTION;
        const branch = coverage.BRANCH;
        const line = coverage.LINE;
        const method = coverage.METHOD;
        const classCov = coverage.CLASS;
        
        if (instruction) {
            markdown.push('### 명령어 커버리지');
            markdown.push(`- 커버됨: ${instruction.covered.toLocaleString()} / ${instruction.total.toLocaleString()}`);
            markdown.push(`- 커버리지: ${instruction.percentage.toFixed(2)}%\n`);
        }
        
        if (branch) {
            markdown.push('### 브랜치 커버리지');
            markdown.push(`- 커버됨: ${branch.covered.toLocaleString()} / ${branch.total.toLocaleString()}`);
            markdown.push(`- 커버리지: ${branch.percentage.toFixed(2)}%\n`);
        }
        
        if (line) {
            markdown.push('### 라인 커버리지');
            markdown.push(`- 커버됨: ${line.covered.toLocaleString()} / ${line.total.toLocaleString()}`);
            markdown.push(`- 커버리지: ${line.percentage.toFixed(2)}%\n`);
        }
        
        if (method) {
            markdown.push('### 메서드 커버리지');
            markdown.push(`- 커버됨: ${method.covered.toLocaleString()} / ${method.total.toLocaleString()}`);
            markdown.push(`- 커버리지: ${method.percentage.toFixed(2)}%\n`);
        }
        
        if (classCov) {
            markdown.push('### 클래스 커버리지');
            markdown.push(`- 커버됨: ${classCov.covered.toLocaleString()} / ${classCov.total.toLocaleString()}`);
            markdown.push(`- 커버리지: ${classCov.percentage.toFixed(2)}%\n`);
        }
        
        markdown.push('### 리포트 다운로드');
        markdown.push('- HTML 리포트는 Artifacts의 `coverage-reports`에서 다운로드할 수 있습니다.');
        markdown.push('- 경로: `build/reports/jacoco/jacocoRootReport/html/index.html`\n');
    } else {
        markdown.push('커버리지 리포트를 생성할 수 없습니다.\n');
    }
    
    return markdown.join('\n');
}

if (require.main === module) {
    const summary = generateMarkdown();
    console.log(summary);
    
    const summaryFile = process.env.GITHUB_STEP_SUMMARY;
    if (summaryFile) {
        fs.appendFileSync(summaryFile, summary);
    }
}

module.exports = { generateMarkdown, parseTestResults, parseCoverage };

